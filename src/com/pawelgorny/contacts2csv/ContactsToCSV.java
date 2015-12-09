package com.pawelgorny.contacts2csv;

import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;

import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.pawelgorny.contacts2csv.translate.TranslateResource;

public class ContactsToCSV extends UiApplication implements TranslateResource{
	public static ResourceBundle _resources = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	com.pawelgorny.contacts2csv.ExportScreen es;
	public static final int ZONE_ID=117539;
	//public static final int ZONE_ID=31848;//test
	//public static final int SITE_ID=25506;
	//private static final String COMMA=",";
	//private static final byte[] COMMA_BYTES=COMMA.getBytes();
	private static final String targetEncoding[] = {null, "ISO-8859-1", "UTF8", "UTF-16BE"};
	private static final String SPACE=" ";
	public static final String NEWLINE1="\n";
	public static final String NEWLINE2="\r\n";
	private static final byte[] NEWLINE1_BYTES=NEWLINE1.getBytes();
	private static final byte[] NEWLINE2_BYTES=NEWLINE2.getBytes();
	private static final int[] fieldsExport=new int[]{
		BlackBerryContact.NAME,
		BlackBerryContact.TEL,
		BlackBerryContact.ORG,
		BlackBerryContact.PIN,
		BlackBerryContact.EMAIL,
		BlackBerryContact.TITLE,
		BlackBerryContact.BIRTHDAY
		};
	
    public static void main(String[] args){
    	
    	ContactsToCSV theApp = new ContactsToCSV();
        theApp.enterEventDispatcher();
    }
    
    public ContactsToCSV(){
    	es=new ExportScreen();
    	pushScreen( es );
    }
    
    
    public static void export(boolean[] params, String separatorString, boolean targetDevice, int selectedEncoding)
    {
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmm");
		String date=sdf.formatLocal(System.currentTimeMillis());
		int step=1;
    	try {
    		String[] contactLists=PIM.getInstance().listPIMLists(PIM.CONTACT_LIST);
    		if (contactLists==null || contactLists.length==0)
    		{
    			Dialog.inform(_resources.getString(Contactsnotfound));
				return;
    		}
    		step=2;
    		String dirName="/store/home/user/contacts2csv/";//contacts"+date+".csv";
    		if (!targetDevice)
    		{
    			dirName="/SDCard/contacts2csv/";
    		}
    		step=3;
			FileConnection fc = (FileConnection)Connector.open("file://"+dirName);
			if (!fc.exists())
            {
				try{
				fc.mkdir();
				}catch (Exception exc){
					if (!targetDevice){
						dirName="/SDCard/contacts2csv/";
						step=4;
						fc = (FileConnection)Connector.open("file://"+dirName);
						if (!fc.exists())
							fc.mkdir();
					}
				}
            }
			step=5;
			int i=0;
			String istr="";
			String filename;
			do{
			filename=dirName.concat("contacts").concat(date).concat(istr).concat(".csv");
			fc = (FileConnection)Connector.open("file://".concat(filename));
			i++;
			istr="_"+String.valueOf(i);
			}while(fc.exists());
			if (!fc.exists())
            {
				fc.create();
            }
			step=6;
			OutputStream outStream = fc.openOutputStream(); 
			byte[] newLine;
	    	if (params[params.length-1])
	    		newLine=NEWLINE1_BYTES;
	    	else newLine=NEWLINE2_BYTES;
	    	step=7;
	    	byte[]SEPARATOR=separatorString.getBytes();
	    	int totalNumber=0;
	    	step=8;
    		for(int listNr=0; listNr<contactLists.length; listNr++){
    			ContactList contactList = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY, contactLists[listNr]);
    			//String[] dataFormats = PIM.getInstance().supportedSerialFormats(PIM.CONTACT_LIST);
    			step=9;
    			Enumeration enumeration = contactList.items();
    			if (false==enumeration.hasMoreElements())
    			{
    				//Dialog.inform(_resources.getString(Contactsnotfound));
    				continue;//return;
    			}
    			step=10;
    			int progress=0;
    			step=101;
    	    	int lastProgress=progress;
    	    	step=102;
    			int numberOfContacts=0;
    			step=103;
    			while(enumeration.hasMoreElements())
    			{
    				step=104;
    				numberOfContacts++;
    				step=105;
    				enumeration.nextElement();
    				step=106;
    			}
    			step=11;
    			enumeration = contactList.items();
    			int contactIx=0;
    			step=12;
    			String encoding=targetEncoding[selectedEncoding];
    			while(enumeration.hasMoreElements())
    			{
    				BlackBerryContact contact=(BlackBerryContact)enumeration.nextElement();
    			    int id;
    			    step=13;
    			     for(int index = 0; index < fieldsExport.length; ++index)
    			     {
    			    	  if (index>0 && !params[index-1])
    			    		  continue;
    			          id = fieldsExport[index];
    			          //String type=contact.getPIMList().getFieldLabel(id);
    			          step=140;
    			          String value=getValue(contact, id, separatorString);
    			          step=150;
    			          if (index>0)
    			        	  outStream.write(SEPARATOR);
    			          if (value!=null){
    			        	  byte[]bytes;
    			        	  if (encoding==null)
    			        		  bytes=value.getBytes();
    			        	  else bytes=value.getBytes(encoding);
    			        	  outStream.write(bytes);
    			          }
    			     }
    			    step=15;
    			    outStream.write(newLine);
    			    step=16;
    				contactIx++;
    				progress=(100*contactIx)/numberOfContacts;
    				if (lastProgress!=progress)
    				{
    				lastProgress=progress;
    				try{
    				synchronized (UiApplication.getEventLock()) {
    					ExportScreen.percentGauge.setValue(lastProgress);
    					UiApplication.getUiApplication().repaint();
    				}
    				}catch (Exception exc){}
    				}//progress update
    			}//while enumeration
    			totalNumber+=contactIx;
    		}//for list
			
			outStream.close();
			fc.close();
			//progress=100;
			Dialog.inform("["+totalNumber+"] "+_resources.getString(exportCompleted)+filename);
		} catch(Exception exc)
		{
			Dialog.inform("Error! step:"+step+" "+exc.getMessage());
		}
    }//export
    
    
    private static String getValue(BlackBerryContact contact, int id, String separator)
    {
    	String value=null;
    	try{
        //String type=contact.getPIMList().getFieldLabel(id);
        int fieldDataType=contact.getPIMList().getFieldDataType(id);
        if(fieldDataType == BlackBerryContact.STRING)
        {
             for(int j=0; j < contact.countValues(id); ++j)
             {
                  String value1 = contact.getString(id, j);
                  if (j>0 && id!=BlackBerryContact.EMAIL)
                  	value+=SPACE+value1;
                  if (j>0 && id==BlackBerryContact.EMAIL)
                    value+=separator+value1;
                  else if(j==0)
                	  	value=value1;
             }
        }//string
        else
        if(fieldDataType == BlackBerryContact.STRING_ARRAY)
        {
      	  String[] values=contact.getStringArray(id, 0);
          	   	for (int v=0; v<values.length; v++)
          	   	{
          	   		String value1 = values[v];
          	   		if (value1!=null)
          	   		{
          	   			if (value!=null && id!=BlackBerryContact.EMAIL)
          	   				value+=SPACE;
          	   			if (value!=null && id==BlackBerryContact.EMAIL)
          	   				value+=separator;
          	   			if (value==null)
          	   				value=value1;
          	   			else value+=value1;
          	   		}
          	   	}
        }//array
        else
        	if(fieldDataType == BlackBerryContact.DATE)
        	{
        		long value1 = contact.getDate(id, 0);
        		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        		value=sdf.format(new Date(value1));
        	}
    	}catch (Exception exc)
    	{
    		return null;
    	}
        return value;
    }//getValue
    
}
