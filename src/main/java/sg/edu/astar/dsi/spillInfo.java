/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sg.edu.astar.dsi;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class spillInfo extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"spillInfo\",\"namespace\":\"sg.edu.astar.dsi\",\"fields\":[{\"name\":\"jobId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"mapperId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"spillFilePath\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"spillIndexPath\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"reduceInfo\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"avro.java.string\":\"String\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.String jobId;
  @Deprecated public java.lang.String mapperId;
  @Deprecated public java.lang.String spillFilePath;
  @Deprecated public java.lang.String spillIndexPath;
  @Deprecated public java.util.Map<java.lang.String,java.lang.String> reduceInfo;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public spillInfo() {}

  /**
   * All-args constructor.
   */
  public spillInfo(java.lang.String jobId, java.lang.String mapperId, java.lang.String spillFilePath, java.lang.String spillIndexPath, java.util.Map<java.lang.String,java.lang.String> reduceInfo) {
    this.jobId = jobId;
    this.mapperId = mapperId;
    this.spillFilePath = spillFilePath;
    this.spillIndexPath = spillIndexPath;
    this.reduceInfo = reduceInfo;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return jobId;
    case 1: return mapperId;
    case 2: return spillFilePath;
    case 3: return spillIndexPath;
    case 4: return reduceInfo;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: jobId = (java.lang.String)value$; break;
    case 1: mapperId = (java.lang.String)value$; break;
    case 2: spillFilePath = (java.lang.String)value$; break;
    case 3: spillIndexPath = (java.lang.String)value$; break;
    case 4: reduceInfo = (java.util.Map<java.lang.String,java.lang.String>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'jobId' field.
   */
  public java.lang.String getJobId() {
    return jobId;
  }

  /**
   * Sets the value of the 'jobId' field.
   * @param value the value to set.
   */
  public void setJobId(java.lang.String value) {
    this.jobId = value;
  }

  /**
   * Gets the value of the 'mapperId' field.
   */
  public java.lang.String getMapperId() {
    return mapperId;
  }

  /**
   * Sets the value of the 'mapperId' field.
   * @param value the value to set.
   */
  public void setMapperId(java.lang.String value) {
    this.mapperId = value;
  }

  /**
   * Gets the value of the 'spillFilePath' field.
   */
  public java.lang.String getSpillFilePath() {
    return spillFilePath;
  }

  /**
   * Sets the value of the 'spillFilePath' field.
   * @param value the value to set.
   */
  public void setSpillFilePath(java.lang.String value) {
    this.spillFilePath = value;
  }

  /**
   * Gets the value of the 'spillIndexPath' field.
   */
  public java.lang.String getSpillIndexPath() {
    return spillIndexPath;
  }

  /**
   * Sets the value of the 'spillIndexPath' field.
   * @param value the value to set.
   */
  public void setSpillIndexPath(java.lang.String value) {
    this.spillIndexPath = value;
  }

  /**
   * Gets the value of the 'reduceInfo' field.
   */
  public java.util.Map<java.lang.String,java.lang.String> getReduceInfo() {
    return reduceInfo;
  }

  /**
   * Sets the value of the 'reduceInfo' field.
   * @param value the value to set.
   */
  public void setReduceInfo(java.util.Map<java.lang.String,java.lang.String> value) {
    this.reduceInfo = value;
  }

  /** Creates a new spillInfo RecordBuilder */
  public static sg.edu.astar.dsi.spillInfo.Builder newBuilder() {
    return new sg.edu.astar.dsi.spillInfo.Builder();
  }
  
  /** Creates a new spillInfo RecordBuilder by copying an existing Builder */
  public static sg.edu.astar.dsi.spillInfo.Builder newBuilder(sg.edu.astar.dsi.spillInfo.Builder other) {
    return new sg.edu.astar.dsi.spillInfo.Builder(other);
  }
  
  /** Creates a new spillInfo RecordBuilder by copying an existing spillInfo instance */
  public static sg.edu.astar.dsi.spillInfo.Builder newBuilder(sg.edu.astar.dsi.spillInfo other) {
    return new sg.edu.astar.dsi.spillInfo.Builder(other);
  }
  
  /**
   * RecordBuilder for spillInfo instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<spillInfo>
    implements org.apache.avro.data.RecordBuilder<spillInfo> {

    private java.lang.String jobId;
    private java.lang.String mapperId;
    private java.lang.String spillFilePath;
    private java.lang.String spillIndexPath;
    private java.util.Map<java.lang.String,java.lang.String> reduceInfo;

    /** Creates a new Builder */
    private Builder() {
      super(sg.edu.astar.dsi.spillInfo.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sg.edu.astar.dsi.spillInfo.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.jobId)) {
        this.jobId = data().deepCopy(fields()[0].schema(), other.jobId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.mapperId)) {
        this.mapperId = data().deepCopy(fields()[1].schema(), other.mapperId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.spillFilePath)) {
        this.spillFilePath = data().deepCopy(fields()[2].schema(), other.spillFilePath);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.spillIndexPath)) {
        this.spillIndexPath = data().deepCopy(fields()[3].schema(), other.spillIndexPath);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.reduceInfo)) {
        this.reduceInfo = data().deepCopy(fields()[4].schema(), other.reduceInfo);
        fieldSetFlags()[4] = true;
      }
    }
    
    /** Creates a Builder by copying an existing spillInfo instance */
    private Builder(sg.edu.astar.dsi.spillInfo other) {
            super(sg.edu.astar.dsi.spillInfo.SCHEMA$);
      if (isValidValue(fields()[0], other.jobId)) {
        this.jobId = data().deepCopy(fields()[0].schema(), other.jobId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.mapperId)) {
        this.mapperId = data().deepCopy(fields()[1].schema(), other.mapperId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.spillFilePath)) {
        this.spillFilePath = data().deepCopy(fields()[2].schema(), other.spillFilePath);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.spillIndexPath)) {
        this.spillIndexPath = data().deepCopy(fields()[3].schema(), other.spillIndexPath);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.reduceInfo)) {
        this.reduceInfo = data().deepCopy(fields()[4].schema(), other.reduceInfo);
        fieldSetFlags()[4] = true;
      }
    }

    /** Gets the value of the 'jobId' field */
    public java.lang.String getJobId() {
      return jobId;
    }
    
    /** Sets the value of the 'jobId' field */
    public sg.edu.astar.dsi.spillInfo.Builder setJobId(java.lang.String value) {
      validate(fields()[0], value);
      this.jobId = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'jobId' field has been set */
    public boolean hasJobId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'jobId' field */
    public sg.edu.astar.dsi.spillInfo.Builder clearJobId() {
      jobId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'mapperId' field */
    public java.lang.String getMapperId() {
      return mapperId;
    }
    
    /** Sets the value of the 'mapperId' field */
    public sg.edu.astar.dsi.spillInfo.Builder setMapperId(java.lang.String value) {
      validate(fields()[1], value);
      this.mapperId = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'mapperId' field has been set */
    public boolean hasMapperId() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'mapperId' field */
    public sg.edu.astar.dsi.spillInfo.Builder clearMapperId() {
      mapperId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'spillFilePath' field */
    public java.lang.String getSpillFilePath() {
      return spillFilePath;
    }
    
    /** Sets the value of the 'spillFilePath' field */
    public sg.edu.astar.dsi.spillInfo.Builder setSpillFilePath(java.lang.String value) {
      validate(fields()[2], value);
      this.spillFilePath = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'spillFilePath' field has been set */
    public boolean hasSpillFilePath() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'spillFilePath' field */
    public sg.edu.astar.dsi.spillInfo.Builder clearSpillFilePath() {
      spillFilePath = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'spillIndexPath' field */
    public java.lang.String getSpillIndexPath() {
      return spillIndexPath;
    }
    
    /** Sets the value of the 'spillIndexPath' field */
    public sg.edu.astar.dsi.spillInfo.Builder setSpillIndexPath(java.lang.String value) {
      validate(fields()[3], value);
      this.spillIndexPath = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'spillIndexPath' field has been set */
    public boolean hasSpillIndexPath() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'spillIndexPath' field */
    public sg.edu.astar.dsi.spillInfo.Builder clearSpillIndexPath() {
      spillIndexPath = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'reduceInfo' field */
    public java.util.Map<java.lang.String,java.lang.String> getReduceInfo() {
      return reduceInfo;
    }
    
    /** Sets the value of the 'reduceInfo' field */
    public sg.edu.astar.dsi.spillInfo.Builder setReduceInfo(java.util.Map<java.lang.String,java.lang.String> value) {
      validate(fields()[4], value);
      this.reduceInfo = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'reduceInfo' field has been set */
    public boolean hasReduceInfo() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'reduceInfo' field */
    public sg.edu.astar.dsi.spillInfo.Builder clearReduceInfo() {
      reduceInfo = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    public spillInfo build() {
      try {
        spillInfo record = new spillInfo();
        record.jobId = fieldSetFlags()[0] ? this.jobId : (java.lang.String) defaultValue(fields()[0]);
        record.mapperId = fieldSetFlags()[1] ? this.mapperId : (java.lang.String) defaultValue(fields()[1]);
        record.spillFilePath = fieldSetFlags()[2] ? this.spillFilePath : (java.lang.String) defaultValue(fields()[2]);
        record.spillIndexPath = fieldSetFlags()[3] ? this.spillIndexPath : (java.lang.String) defaultValue(fields()[3]);
        record.reduceInfo = fieldSetFlags()[4] ? this.reduceInfo : (java.util.Map<java.lang.String,java.lang.String>) defaultValue(fields()[4]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}