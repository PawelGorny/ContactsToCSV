package com.pawelgorny.contacts2csv;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.MainScreen;
import net.rimlib.blackberry.api.advertising.app.Banner;
import net.rimlib.blackberry.api.paymentsdk.PaymentEngine;
import net.rimlib.blackberry.api.paymentsdk.PaymentException;
import net.rimlib.blackberry.api.paymentsdk.PurchaseArgumentsBuilder;
import net.rimlib.blackberry.api.paymentsdk.PurchaseResult;

import com.pawelgorny.contacts2csv.translate.TranslateResource;

public class ExportScreen extends MainScreen {
	
	static GaugeField percentGauge;
//	private Timer timer=new Timer();
	private boolean saving=false;
	private boolean progressVisible=false;
	private final CheckboxField checkBoxName = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.name), true);
	private final CheckboxField checkBoxPhone = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.phone), true);
	private final CheckboxField checkBoxOrg = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.organisation), true);
	private final CheckboxField checkBoxPIN = new CheckboxField("Blackberry PIN", true);
	private final CheckboxField checkBoxEmail = new CheckboxField("email", true);
	private final CheckboxField checkBoxBirthday = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.Birthdate), false);
	//private final CheckboxField checkBoxNickName = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.nickName), false);
	//private final CheckboxField checkBoxRevDate = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.revDate), false);
	private final CheckboxField checkBoxTitle = new CheckboxField(ContactsToCSV._resources.getString(TranslateResource.exTitle), false);
	private final String choices[] = {"\\r\\n", "\\n"};
	private final ObjectChoiceField newline=new ObjectChoiceField(ContactsToCSV._resources.getString(TranslateResource.newLine),choices, 0);
	
	private final String sepChoices[] = {",", "|", ";", "\\t"};
	private final ObjectChoiceField separator=new ObjectChoiceField(ContactsToCSV._resources.getString(TranslateResource.separator), sepChoices, 0);
	
	private final String targetChoices[] = {"Device", "SDCard"};
	private final ObjectChoiceField target=new ObjectChoiceField(ContactsToCSV._resources.getString(TranslateResource.target),targetChoices, 0);
	
	private final String targetEncoding[] = {"default", "ISO-8859-1", "UTF-8", "UTF-16"};
	private final ObjectChoiceField encodingSelect=new ObjectChoiceField(ContactsToCSV._resources.getString(TranslateResource.encoding),targetEncoding, 0);
	
	private Banner advBanner=new Banner(ContactsToCSV.ZONE_ID, null);
	
	PurchaseArgumentsBuilder purchaseArguments;

	public ExportScreen() {
	 super( MainScreen.VERTICAL_SCROLL | MainScreen.VERTICAL_SCROLLBAR );
	 ApplicationDescriptor desc= ApplicationDescriptor.currentApplicationDescriptor();
     setTitle( ContactsToCSV._resources.getString(TranslateResource.title)
    		 +(desc!=null?(" "+ desc.getVersion()):"") );
     
     advBanner.setMMASize(Banner.MMA_SIZE_AUTO);
     advBanner.setTestModeFlag(false);
     add(advBanner);
     checkBoxName.setEditable(false);
     add(checkBoxName); 
     add(checkBoxPhone);
     add(checkBoxOrg);
     add(checkBoxPIN);
     add(checkBoxEmail);
     //add(checkBoxNickName);
     //add(checkBoxRevDate);
     add(checkBoxTitle);
     add(checkBoxBirthday);
     add(newline);
     //percentGauge.setVisualState(VISUAL_STATE_DISABLED);
     //add(percentGauge);
     add(separator);
     add(target);
     add(encodingSelect);
     percentGauge = new GaugeField(ContactsToCSV._resources.getString(TranslateResource.progress), 0, 100, 0, GaugeField.PERCENT);
     add(percentGauge);
	ButtonField buttonField_1 = new ButtonField( "Start!", ButtonField.CONSUME_CLICK | ButtonField.FIELD_HCENTER );
    add( buttonField_1 );
    buttonField_1.setChangeListener( new FieldChangeListener() {
        public void fieldChanged( Field arg0, int arg1 ) {
        	//timer.schedule(timerTask(), 100,100);
        	runExport();
        }
    } );
    
    addMenuItem(_startItem);
    
    ButtonField buttonDonate = new ButtonField( "Please donate $0.99", ButtonField.CONSUME_CLICK | ButtonField.FIELD_HCENTER );
    add( buttonDonate );
    buttonDonate.setChangeListener( new FieldChangeListener() {
        public void fieldChanged( Field arg0, int arg1 ) {
        	try 
        	{
        		PaymentEngine engine=PaymentEngine.getInstance();
        	    PurchaseResult purchaseResult = engine.purchase(purchaseArguments.build());
        	    if (purchaseResult.isSuccessful()){
        	    	Dialog.inform(ContactsToCSV._resources.getString(TranslateResource.THANKYOU));
        	    }
        	} 
        	catch (PaymentException e) 
        	{
        	    Dialog.inform(e.getMessage());
        	}
        }
    } );
    
    purchaseArguments = new PurchaseArgumentsBuilder()
    .withDigitalGoodSku( "23204875" )
    .withDigitalGoodName( "Donate USD0.99" )
    .withPurchasingAppName( "Contacts to CSV" );
	}//constructor
	
	public void runExport(){
		if (progressVisible==false)
    	{
    	synchronized (UiApplication.getEventLock()) {
    		//percentGauge.setVisualState(VISUAL_STATE_NORMAL);
    		//add(percentGauge);
			UiApplication.getUiApplication().repaint();
			progressVisible=true;
		}
    	}
    	boolean[] params=new boolean[7];
    	params[0]=checkBoxPhone.getChecked();
    	params[1]=checkBoxOrg.getChecked();
    	params[2]=checkBoxPIN.getChecked();
    	params[3]=checkBoxEmail.getChecked();
    	params[4]=checkBoxTitle.getChecked();
    	params[5]=checkBoxBirthday.getChecked();
    	params[6]=(newline.getSelectedIndex()==1);
    	String sep=sepChoices[separator.getSelectedIndex()];
    	if (sep.equals("\\t"))
    		sep="\t";
        ContactsToCSV.export(params,  sep, (target.getSelectedIndex()==0), encodingSelect.getSelectedIndex());
        //timer.cancel();
	}
	
	private MenuItem _startItem = new MenuItem("Start export", 110, 10)
    {
        public void run()
        {
        	runExport();
        }
    };
	
/*		
	private TimerTask timerTask(){
		TimerTask tt=new TimerTask() {
			public void run() {
				if (ContactsToCSV.progress==100)
				{
					try{
						percentGauge.setValue(ContactsToCSV.progress);
						cancel();
						timer.cancel();
					}catch (Exception e){}
				}
				else
				{
					synchronized (UiApplication.getEventLock()) {
						percentGauge.setValue(ContactsToCSV.progress);
						UiApplication.getUiApplication().repaint();
					}
				}
			}
		};
		return tt;
	}
*/	
	protected boolean onSavePrompt(){
		if (saving){
			saving=false;
			return onSave();
		}
		return true;
	}
	
}
