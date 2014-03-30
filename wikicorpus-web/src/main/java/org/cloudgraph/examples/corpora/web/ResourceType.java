package org.cloudgraph.examples.wikicorpus.web;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;


public class ResourceType 
{
      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    public static final int LABEL_TYPE      = 0;
    public static final int TITLE_TYPE      = 1;
    public static final int ICON_TYPE       = 2;
    public static final int TOOLTIP_TYPE    = 3;
    public static final int DESCR_TYPE      = 4;
    public static final int CAPTION_TYPE    = 5;
    public static final int HELP_TYPE       = 6;
    public static final int PATTERN_TYPE       = 7;

    public static final ResourceType LABEL = new ResourceType(LABEL_TYPE, "label");
    public static final ResourceType TITLE = new ResourceType(LABEL_TYPE, "title");
    public static final ResourceType ICON = new ResourceType(ICON_TYPE, "icon");
    public static final ResourceType TOOLTIP = new ResourceType(TOOLTIP_TYPE, "tooltip");
    public static final ResourceType DESCR = new ResourceType(DESCR_TYPE, "descr");
    public static final ResourceType CAPTION = new ResourceType(CAPTION_TYPE, "caption");
    public static final ResourceType HELP = new ResourceType(HELP_TYPE, "help");
    public static final ResourceType PATTERN = new ResourceType(PATTERN_TYPE, "pattern");



    private static java.util.Hashtable _memberTable = init();

    private int type = -1;

    private java.lang.String stringValue = null;


      //----------------/
     //- Constructors -/
    //----------------/

    private ResourceType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.cloudgraph.web.ResourceType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns an enumeration of all possible instances of
     * ResourceType
    **/
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Returns the type of this ResourceType
    **/
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
    **/
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("label", LABEL);
        members.put("title", TITLE);
        members.put("icon", ICON);
        members.put("tooltip", TOOLTIP);
        members.put("descr", DESCR);
        members.put("caption", CAPTION);
        members.put("help", HELP);
        members.put("pattern", PATTERN);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Returns the String representation of this
     * ResourceType
    **/
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Returns a new ResourceType based on the given
     * String value.
     * 
     * @param string
    **/
    public static ResourceType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid ResourceType";
            throw new IllegalArgumentException(err);
        }
        return (ResourceType) obj;
    } //-- org.cloudgraph.web.ResourceType valueOf(java.lang.String) 

}
