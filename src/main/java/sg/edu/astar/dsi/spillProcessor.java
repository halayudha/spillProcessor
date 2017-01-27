/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.edu.astar.dsi;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.IFile;
import org.apache.hadoop.mapred.IFile.Writer;
import org.apache.hadoop.mapred.IndexRecord;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Merger;
import org.apache.hadoop.mapred.Merger.Segment;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapred.SpillRecord;
import org.apache.hadoop.mapreduce.CryptoUtils;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.apache.hadoop.io.TextDsi;

/**
 *
 * @author hduser
 */
public class spillProcessor {
    
    private static class Worker extends Thread{
        private Context context;
        private Worker (Context context){
            this.context = context;
        }
        
        @Override
        public void run(){
            ZMQ.Socket socket = context.socket(ZMQ.REP);
            socket.connect("inproc://workers");
            //socket.send("world", 0);
            while (true){
                byte[] request = socket.recv(0);
                socket.send("world",0);
                SpecificDatumReader<spillInfo> reader = new SpecificDatumReader<spillInfo>(spillInfo.getClassSchema());
                Decoder decoder = DecoderFactory.get().binaryDecoder(request, null);
                try {
                    spillInfo sInfo = reader.read(null, decoder);
                    //System.out.println("sInfo mapperID: " + sInfo.getMapperId());
                    //System.out.println("sInfo spillFilePath: " + sInfo.getSpillFilePath());
                    //System.out.println("sInfo indexFilePath: " + sInfo.getSpillIndexPath());
                    //sendFile(sInfo.getSpillFilePath().toString());
                    
                    doCheckPoint(sInfo.getSpillIndexPath().toString(),
                                 sInfo.getSpillFilePath().toString(),
                                 sInfo.getTaskId(), //TASKID
                                 sInfo.getMapperId(),//MAPPERID-ATTEMPT
                                 sInfo.getNumSpills()
                                 
                                 );
                    
                    getSpillPartitionThread doSend = new getSpillPartitionThread(
                                      sInfo.getJobId(),
                                      sInfo.getMapperId(),
                                      sInfo.getSpillIndexPath().toString(),
                                      sInfo.getSpillFilePath().toString(),
                                      sInfo.getReduceInfo()
                    );
                    
                    doSend.run();
                    
                    /*
                    getSpillPartition(
                                      sInfo.getJobId(),
                                      sInfo.getMapperId(),
                                      sInfo.getSpillIndexPath().toString(),
                                      sInfo.getSpillFilePath().toString(),
                                      sInfo.getReduceInfo()
                                      );*/
                    /*
                    try {
                        sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(spillProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                } catch (IOException ex) {
                    Logger.getLogger(spillProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
                /*
                String request = socket.recvStr(0);
                                System.out.println(Thread.currentThread().getName() + 
                        " Received request: [" + request + "]");*/
                //socket.send("world", 0);
            }
            
        }
        
        private void sendFile(String jobID, String mapperID, String theFile, int partition_no, String theHost) throws IOException{
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://" + theHost + ":8081" + "/fileupload");
            //HttpPost httppost = new HttpPost("http://192.168.37.225" + ":8081" + "/fileupload");
            FileBody bin = new FileBody(new File(theFile));
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addTextBody("jobID", jobID)
                    .addTextBody("mapperID", mapperID)
                    .addTextBody("partitionNo",String.valueOf(partition_no))
                    .addPart("bin",bin)
                    .build();
            httppost.setEntity(reqEntity);
            
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null){
                System.out.println("Response content length: " + resEntity.getContentLength());
                
            }
            EntityUtils.consume(resEntity);
            response.close();
            httpclient.close();
           }
        
        private void getSpillPartition(String jobID, String mapperID, String IndexFile, String SpillFile, Map<String,String> reduceInfo) throws IOException{
            JobConf job = new JobConf();
            job.setMapOutputKeyClass(TextDsi.class);
            job.setMapOutputValueClass(IntWritable.class);
            Class<TextDsi> keyClass = (Class<TextDsi>)job.getMapOutputKeyClass();
            Class<IntWritable> valClass = (Class<IntWritable>)job.getMapOutputValueClass();
            FileSystem rfs;
            CompressionCodec codec = null;
            Counters.Counter spilledRecordsCounter = null;
            rfs =((LocalFileSystem)FileSystem.getLocal(job)).getRaw();
        
            //GET INFORMATION FROM INDEXFILE
            Path indexFilePath = new Path(IndexFile);
            SpillRecord sr = new SpillRecord(indexFilePath, job);
            System.out.println("indexfile partition size() : " + sr.size());
        
            long startOffset = 0;
            Path spillFilePath = new Path(SpillFile);
            Segment<TextDsi, IntWritable> s = null;
            
            //LOCKIGN MECHANISM
            /*
            File fileSpill = new File (SpillFile);
            FileChannel fileChannel = new RandomAccessFile(fileSpill, "rw").getChannel();
            FileLock lock = fileChannel.lock();
            System.out.println("fileSpill: " + fileSpill.getName() + " is LOCKED!"); 
             */
//Handling spillFile, We only have one spillFile
            String[] tempName = SpillFile.split(Pattern.quote("."));//take out the extension ".out"
            String[] SpillFileName = tempName[0].split(Pattern.quote("/"));
            
        
            List<Segment<TextDsi,IntWritable>> segmentList = new ArrayList<>();
            for (int i = 0;i<sr.size();i++){ //sr.size is the number of partitions
                IndexRecord ir = sr.getIndex(i);
                System.out.println("index[" + i + "] rawLength = " + ir.rawLength);
                System.out.println("index[" + i + "] partLength = " + ir.partLength);
                System.out.println("index[" + i + "] startOffset= " + ir.startOffset);
                startOffset = ir.startOffset;
                //Take out ext.
                /*
                System.out.println(SpillFile);
                String[] tempName = SpillFile.split(Pattern.quote("."));
                System.out.println("name[0]: " + tempName[0]);
                String[] SpillFileName = tempName[0].split(Pattern.quote("/"));
                System.out.println("SpillFileName Length: " + SpillFileName.length);
                System.out.println("SpillFileName: " + SpillFileName[SpillFileName.length-1]);
                */
                
                //The output stream for the final output file
                Path spillPartitionFile = new Path("/home/hduser/temp/" + SpillFileName[SpillFileName.length-1] +"_p_" + i +".out");
                Path spillPartitionIndexFile = new Path("/home/hduser/temp/" + SpillFileName[SpillFileName.length-1] + "_p_" + i +".out.index");
                FSDataOutputStream spillPartitionFileOut = rfs.create(spillPartitionFile, true, 4096);
                
                
                s = new Segment<>(job, rfs,spillFilePath,
                                    ir.startOffset,
                                    ir.partLength,
                                    codec,
                                    true
                                    );
                segmentList.add(0,s);
                
                RawKeyValueIterator kvIter = Merger.merge(job, 
                                                    rfs, 
                                                    keyClass, 
                                                    valClass, 
                                                    null, //codec
                                                  segmentList,
                                                  10,//mergeFactor
                                                  new Path("/home/hduser/temp"),
                                                  job.getOutputKeyComparator(),//job.getOutputKeyComparator
                                                  null,//reporter
                                                  false,//boolean sortSegments
                                                  null,//null
                                                  spilledRecordsCounter,
                                                  null,//sortPhase.phase()
                                                  TaskType.MAP);
                
                 //lock.release();
                
                //write to disk
                long segmentStart = spillPartitionFileOut.getPos();
                FSDataOutputStream finalSpillPartitionFileOut = CryptoUtils.wrapIfNecessary(job, spillPartitionFileOut);
                System.out.println("GOT2A");
                Writer <TextDsi,IntWritable> writer = new Writer<TextDsi,IntWritable>(job, finalSpillPartitionFileOut, TextDsi.class, IntWritable.class, codec,
                                                    spilledRecordsCounter);
                System.out.println("GOT2J");
                Merger.writeFile(kvIter, writer, null, job);
                System.out.println("GOT3");
                writer.close();
               /* 
                lock.release();
                System.out.println("file: " + fileSpill.getName() + " is RELEASED!");
                */
                //Creating Index File
                IndexRecord rec = new IndexRecord();
                rec.startOffset = segmentStart;
                rec.rawLength = writer.getRawLength() + CryptoUtils.cryptoPadding(job);
                rec.partLength = writer.getCompressedLength() + CryptoUtils.cryptoPadding(job);
                System.out.println("rec.startOffset: " + rec.startOffset);
                System.out.println("rec.rawLength  : " + rec.rawLength);
                System.out.println("rec.partLength : " + rec.partLength);
                
                final SpillRecord spillRec = new SpillRecord(1);
                spillRec.putIndex(rec, 0);
                spillRec.writeToFile(spillPartitionIndexFile, job);
                
               
                //Sending New Spill File
                System.out.println("SENDING: " + spillPartitionFile.toString() + "TO: " + reduceInfo.get(String.valueOf(i)));
                sendFile(jobID, mapperID, spillPartitionFile.toString(), i , reduceInfo.get(String.valueOf(i)));
                //Sending New Index File
                System.out.println("SENDING: " + spillPartitionIndexFile.toString());
                sendFile(jobID, mapperID, spillPartitionIndexFile.toString(), i , reduceInfo.get(String.valueOf(i)));
                spillPartitionFileOut.close();//Close the newly created spill file
              
                //rfs.delete(spillPartitionFile);
                //rfs.delete(spillPartitionIndexFile);
                      
        }//END FOR LOOP FOR NUMBER OF PARTITIONS.
      
        
        
        
        
        }
        
        private void doCheckPoint(String IndexFile, 
                                  String SpillFile,
                                  String TaskID,
                                  String MapperID,
                                  int numSpills) throws IOException{
            JobConf myjob = new JobConf();
            myjob.setMapOutputKeyClass(TextDsi.class);
            myjob.setMapOutputValueClass(IntWritable.class);
            Class<TextDsi> mykeyClass = (Class<TextDsi>)myjob.getMapOutputKeyClass();
            Class<IntWritable> myvalClass = (Class<IntWritable>)myjob.getMapOutputValueClass();
            CompressionCodec codec = null;
            FileSystem rfs =((LocalFileSystem)FileSystem.getLocal(myjob)).getRaw();
            Counters.Counter spilledRecordsCounter = null;
            
            Path indexReadPath = new Path(IndexFile);
            SpillRecord sr1 = new SpillRecord(indexReadPath, myjob);
            System.out.println("SpillRecord Size: " + sr1.size());
            
            Path mapOutputFileNamePath2 = new Path(SpillFile);
        
            List<Segment<TextDsi,IntWritable>> mySegmentList = new ArrayList<>();
            
            IndexRecord ir = null;
            for (int i = 0 ; i < sr1.size(); i++){
                ir = sr1.getIndex(i);
                System.out.println("startOffset[" + i +"] = " + ir.startOffset);
                Segment<TextDsi,IntWritable> mySegment = new Segment<>(myjob,rfs,mapOutputFileNamePath2,
                                            ir.startOffset,
                                            ir.partLength,
                                            codec,
                                            true);
                mySegmentList.add(i,mySegment);
        }
        RawKeyValueIterator mykvIter = Merger.merge(myjob, 
                                                    rfs, 
                                                    mykeyClass, 
                                                    myvalClass, 
                                                    null, //codec
                                                  mySegmentList,
                                                  10,//mergeFactor
                                                  new Path("/home/hduser/temp"),
                                                  myjob.getOutputKeyComparator(),//job.getOutputKeyComparator
                                                  null,//reporter
                                                  false,//boolean sortSegments
                                                  null,//null
                                                  spilledRecordsCounter,
                                                  null,//sortPhase.phase()
                                                  TaskType.MAP);   
            
        Merger.DSIGetSpillCheckPoint(mykvIter, 
                                        myjob,
                                        TaskID, 
                                        MapperID,
                                        mapOutputFileNamePath2.getName(),
                                        numSpills);
            
            
            
        }
        
    }

    private static class getSpillPartitionThread extends Thread{
        private String jobID;
        private String mapperID;
        private String IndexFile;
        private String SpillFile;
        private Map<String, String> reduceInfo;
  
        
        public getSpillPartitionThread(String jobID, String mapperID, String IndexFile, String SpillFile, Map<String,String> reduceInfo){
                this.jobID = jobID;
                this.mapperID = mapperID;
                this.IndexFile = IndexFile;
                this.SpillFile = SpillFile;
                this.reduceInfo = reduceInfo;
        }
        @Override
        public void run(){
            try {
                JobConf job = new JobConf();
                job.setMapOutputKeyClass(TextDsi.class);
                job.setMapOutputValueClass(IntWritable.class);
                Class<TextDsi> keyClass = (Class<TextDsi>)job.getMapOutputKeyClass();
                Class<IntWritable> valClass = (Class<IntWritable>)job.getMapOutputValueClass();
                FileSystem rfs;
                CompressionCodec codec = null;
                Counters.Counter spilledRecordsCounter = null;
                rfs =((LocalFileSystem)FileSystem.getLocal(job)).getRaw();
               
                //GET INFORMATION FROM INDEXFILE
                Path indexFilePath = new Path(IndexFile);
                SpillRecord sr = new SpillRecord(indexFilePath, job);
                System.out.println("indexfile partition size() : " + sr.size());
                
                long startOffset = 0;
                Path spillFilePath = new Path(SpillFile);
                Segment<TextDsi, IntWritable> s = null;
                
                String[] tempName = SpillFile.split(Pattern.quote("."));//take out the extension ".out"
                String[] SpillFileName = tempName[0].split(Pattern.quote("/"));
            
        
                List<Segment<TextDsi,IntWritable>> segmentList = new ArrayList<>();
                for (int i = 0;i<sr.size();i++){ //sr.size is the number of partitions
                IndexRecord ir = sr.getIndex(i);
                System.out.println("index[" + i + "] rawLength = " + ir.rawLength);
                System.out.println("index[" + i + "] partLength = " + ir.partLength);
                System.out.println("index[" + i + "] startOffset= " + ir.startOffset);
                startOffset = ir.startOffset;
                //Take out ext.
                /*
                System.out.println(SpillFile);
                String[] tempName = SpillFile.split(Pattern.quote("."));
                System.out.println("name[0]: " + tempName[0]);
                String[] SpillFileName = tempName[0].split(Pattern.quote("/"));
                System.out.println("SpillFileName Length: " + SpillFileName.length);
                System.out.println("SpillFileName: " + SpillFileName[SpillFileName.length-1]);
                */
                
                //The output stream for the final output file
                Path spillPartitionFile = new Path("/home/hduser/temp/" + SpillFileName[SpillFileName.length-1] +"_p_" + i +".out");
                Path spillPartitionIndexFile = new Path("/home/hduser/temp/" + SpillFileName[SpillFileName.length-1] + "_p_" + i +".out.index");
                FSDataOutputStream spillPartitionFileOut = rfs.create(spillPartitionFile, true, 4096);
                
                
                s = new Segment<>(job, rfs,spillFilePath,
                                    ir.startOffset,
                                    ir.partLength,
                                    codec,
                                    true
                                    );
                segmentList.add(0,s);
                
                RawKeyValueIterator kvIter = Merger.merge(job, 
                                                    rfs, 
                                                    keyClass, 
                                                    valClass, 
                                                    null, //codec
                                                  segmentList,
                                                  10,//mergeFactor
                                                  new Path("/home/hduser/temp"),
                                                  job.getOutputKeyComparator(),//job.getOutputKeyComparator
                                                  null,//reporter
                                                  false,//boolean sortSegments
                                                  null,//null
                                                  spilledRecordsCounter,
                                                  null,//sortPhase.phase()
                                                  TaskType.MAP);
                
                 //lock.release();
                
                //write to disk
                long segmentStart = spillPartitionFileOut.getPos();
                FSDataOutputStream finalSpillPartitionFileOut = CryptoUtils.wrapIfNecessary(job, spillPartitionFileOut);
                System.out.println("GOT2A");
                Writer <TextDsi,IntWritable> writer = new Writer<TextDsi,IntWritable>(job, finalSpillPartitionFileOut, TextDsi.class, IntWritable.class, codec,
                                                    spilledRecordsCounter);
                System.out.println("GOT2J");
                Merger.writeFile(kvIter, writer, null, job);
                System.out.println("GOT3");
                writer.close();
               /* 
                lock.release();
                System.out.println("file: " + fileSpill.getName() + " is RELEASED!");
                */
                //Creating Index File
                IndexRecord rec = new IndexRecord();
                rec.startOffset = segmentStart;
                rec.rawLength = writer.getRawLength() + CryptoUtils.cryptoPadding(job);
                rec.partLength = writer.getCompressedLength() + CryptoUtils.cryptoPadding(job);
                System.out.println("rec.startOffset: " + rec.startOffset);
                System.out.println("rec.rawLength  : " + rec.rawLength);
                System.out.println("rec.partLength : " + rec.partLength);
                
                final SpillRecord spillRec = new SpillRecord(1);
                spillRec.putIndex(rec, 0);
                spillRec.writeToFile(spillPartitionIndexFile, job);
                
               
                //Sending New Spill File
                System.out.println("SENDING: " + spillPartitionFile.toString() + "TO: " + reduceInfo.get(String.valueOf(i)));
                sendFile(jobID, mapperID, spillPartitionFile.toString(), i , reduceInfo.get(String.valueOf(i)));
                //Sending New Index File
                System.out.println("SENDING: " + spillPartitionIndexFile.toString());
                sendFile(jobID, mapperID, spillPartitionIndexFile.toString(), i , reduceInfo.get(String.valueOf(i)));
                spillPartitionFileOut.close();//Close the newly created spill file
              
                //rfs.delete(spillPartitionFile);
                //rfs.delete(spillPartitionIndexFile);
                      
        }//END FOR LOOP FOR NUMBER OF PARTITIONS.
                
                
                
            } catch (IOException ex) {
                Logger.getLogger(spillProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//END RUN()
        
        private void sendFile(String jobID, String mapperID, String theFile, int partition_no, String theHost) throws IOException{
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://" + theHost + ":8081" + "/fileupload");
            //HttpPost httppost = new HttpPost("http://192.168.37.225" + ":8081" + "/fileupload");
            FileBody bin = new FileBody(new File(theFile));
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addTextBody("jobID", jobID)
                    .addTextBody("mapperID", mapperID)
                    .addTextBody("partitionNo",String.valueOf(partition_no))
                    .addPart("bin",bin)
                    .build();
            httppost.setEntity(reqEntity);
            
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null){
                System.out.println("Response content length: " + resEntity.getContentLength());
                
            }
            EntityUtils.consume(resEntity);
            response.close();
            httpclient.close();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket clients = context.socket(ZMQ.ROUTER);
        //clients.bind("tcp://*:5555");
        clients.bind("ipc://home/hduser/mappersocket");
        ZMQ.Socket workers = context.socket(ZMQ.DEALER);
        workers.bind("inproc://workers");
        
        for (int thread_nbr = 0; thread_nbr < 100; thread_nbr++){
            Thread worker = new Worker (context);
            worker.start();
        }
        
        //Connect work threads to client threads via a queue
        ZMQ.proxy(clients, workers, null);
        
        clients.close();
        workers.close();
        context.term();
    }
    
}
