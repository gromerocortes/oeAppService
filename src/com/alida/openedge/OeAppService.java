package com.alida.openedge;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.progress.open4gl.javaproxy.*; 
import com.progress.open4gl.ConnectException;
import com.progress.open4gl.Open4GLException;
import com.progress.open4gl.RunTime4GLException;
import com.progress.open4gl.SystemErrorException;
import com.sonicsw.xq.*;

public class OeAppService implements XQServiceEx {

	
	// This is the XQLog (the container's logging mechanism).
    private XQLog m_xqLog = null;

    //This is the the log prefix that helps identify this service during logging
    private String m_logPrefix = "";
    
    //These hold version information.
    private static int s_major = 1;
    private static int s_minor = 0;
    private static int s_buildNumber = 0;

	private String	SettingUrl;
	private String	SettingBroker;
	private String	SettingProgram;
	
	private OpenAppObject dynAO;
	private ParamArray parms;
	private String Respuesta;
    // Create a place for RETURN-VALUE
    private String retVal;
    private String CustName;

    /**
     * Constructor for a OeAppService
     */
	public OeAppService () {	
	}
	
	/**
     * Initialize the XQService by processing its initialization parameters.
     *
     * <p> This method implements a required XQService method.
     *
     * @param initialContext The Initial Service Context provides access to:<br>
     * <ul>
     * <li>The configuration parameters for this instance of the oeAppService.</li>
     * <li>The XQLog for this instance of the oeAppService.</li>
     * </ul>
     * @exception XQServiceException Used in the event of some error.
     */
    public void init(XQInitContext initialContext) throws XQServiceException {
    	XQParameters params = initialContext.getParameters();
        m_xqLog = initialContext.getLog();
        setLogPrefix(params);
        m_xqLog.logInformation(m_logPrefix + " Initializing ...");

        writeStartupMessage(params);
        //Perform initialization work.
	        
        SettingUrl = params.getParameter("url", 1);
        SettingBroker = params.getParameter("brokerName", 1); 
        SettingProgram = params.getParameter("programName", 1); 

        writeParameters(params);
        
        m_xqLog.logInformation(m_logPrefix +" Initialized ...");
        
        try {
            Connection myConn = new Connection("","","");
			dynAO = new OpenAppObject(myConn, SettingBroker);
			
            CustName = null;
            // Create the ParamArray
            parms = new ParamArray(2);
            
            //////////////////////////////////////
            //Instant start = Instant.now();
			
			
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Open4GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }


    /**
     * Handle the arrival of XQMessages in the INBOX.
     *
     * <p> This method implements a required XQService method.
     *
     * @param ctx The service context.
     * @exception XQServiceException Thrown in the event of a processing error.
     */
    public void service(XQServiceContext ctx) throws XQServiceException {
		m_xqLog.logDebug(m_logPrefix + "Service processing...");

		// Get the message.
		XQEnvelope env = ctx.getNextIncoming();
		if (env != null) {
			XQMessage msg = env.getMessage();
			try {
				int iPartCnt = msg.getPartCount();
				for (int i = 0; i < iPartCnt; i++) {
					XQPart prt = msg.getPart(i);
					Object content = prt.getContent();
                    //m_xqLog.logInformation("Content at Part [" + i + "]:\n" + content);
                    CustName = (String) content;
                    //// OpenEdge
                    try {
                        // Set up input parameters
                        parms.addCharacter(0, CustName, ParamArrayMode.INPUT);
                        // Set up Out parameters - notice the value is null
                        parms.addCharacter(1, null, ParamArrayMode.OUTPUT);
                    	/////
						dynAO.runProc(SettingProgram, parms);
						Respuesta = (String) parms.getOutputParameter(1);
		                // Get RETURN-VALUE - Will return null for AddCustomer() procedure
		                retVal = (String)(parms.getProcReturnString());
		                XQPart p1 = msg.createPart();
		                p1.setContent(Respuesta, "text/plain");
		                msg.addPart(p1);
		                
		                //Una posibilidad en cuanto a tiempo, se tendría que probar
		                //Revisar que es mas rápido, crear una nueva parte o reemplazarla
		                //msg.replacePart(prt, Respuesta);
		                
		                //System.out.println("Salida " + Respuesta);
		                //prt.setContent(env, retVal);
					} catch (RunTime4GLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SystemErrorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Open4GLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (XQMessageException me) {
				throw new XQServiceException("Exception accessing XQMessage: "
						+ me.getMessage(), me);
			}

			// Pass message onto the outbox.
			Iterator<XQAddress> addressList = env.getAddresses();
			if (addressList.hasNext()) {
				// Add the message to the Outbox
				ctx.addOutgoing(env);
			}
		}
		m_xqLog.logDebug(m_logPrefix + "Service processed...");
	}
    
    /**
     * Clean up and get ready to destroy the service.
     *
     * <p> This method implement a required XQService method.
     */
    public void destroy() {
		m_xqLog.logInformation(m_logPrefix + "Destroying...");
		try {
			dynAO._release();
		} catch (SystemErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Open4GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_xqLog.logInformation(m_logPrefix + "Destroyed...");
    }
    
    /**
     * Called by the container on container start.
     *
     * <p> This method implement a required XQServiceEx method.
     */
	public void start() {
		m_xqLog.logInformation(m_logPrefix + "Starting...");
		m_xqLog.logInformation(m_logPrefix + "Started...");
	}
	
    /**
     * Called by the container on container stop.
     *
     * <p> This method implement a required XQServiceEx method.
     */
	public void stop() {
		m_xqLog.logInformation(m_logPrefix + "Stopping...");
		m_xqLog.logInformation(m_logPrefix + "Stopped...");
	}
	
	/**
     * Sets the prefix for XQLog for this instance of the service oeAppService
     */
	protected void setLogPrefix(XQParameters params) {
		String serviceName = params.getParameter(XQConstants.PARAM_SERVICE_NAME, XQConstants.PARAM_STRING);
		m_logPrefix = "[ " +  serviceName + " ]";
	}
	
    /**
     * Provide access to the service implemented version.
     *
     */
	protected String getVersion(){
		return s_major + "." + s_minor + ". build " + s_buildNumber;  
	}
	
    /**
     * Writes a standard service startup message to the log.
     */
    protected void writeStartupMessage(XQParameters params) {
        final StringBuffer buffer = new StringBuffer();
        String serviceTypeName = params.getParameter(XQConstants.SERVICE_PARAM_SERVICE_TYPE, XQConstants.PARAM_STRING);
        buffer.append("\n\n");
        buffer.append("\t\t " + serviceTypeName + "\n ");
        buffer.append("\t\t Version ");
        buffer.append(" " +getVersion());
        buffer.append("\n");
        m_xqLog.logInformation(buffer.toString());
	}
    
    /**
     * Writes parameters to log.
     */
    protected void writeParameters(XQParameters params) {
        final Map<String,XQParameterInfo> map = params.getAllInfo();
        final Iterator<XQParameterInfo> iter = map.values().iterator();

        while (iter.hasNext()) {
            final XQParameterInfo info = (XQParameterInfo) iter.next();

            if (info.getType() == XQConstants.PARAM_XML) {
                m_xqLog.logInformation( m_logPrefix + "Parameter Name =  " + info.getName());
            } else if (info.getType() == XQConstants.PARAM_STRING) {
            	m_xqLog.logInformation( m_logPrefix + "Parameter Name = " + info.getName());
            }

            if (info.getRef() != null) {
            	m_xqLog.logInformation(m_logPrefix +"Parameter Reference = " + info.getRef());

            	//If this is too verbose
            	///then a simple change from logInformation to logDebug
            	//will ensure file content is not displayed
            	//unless the logging level is set to debug for the ESB Container.
            	m_xqLog.logInformation(m_logPrefix +"----Parameter Value Start--------");
            	m_xqLog.logInformation("\n" +  info.getValue() +"\n");
            	m_xqLog.logInformation(m_logPrefix +"----Parameter Value End--------");
            }else{
            	m_xqLog.logInformation(m_logPrefix +"Parameter Value = " + info.getValue());            	
            }
        }
    }
}