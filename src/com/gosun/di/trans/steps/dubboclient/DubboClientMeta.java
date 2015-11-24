package com.gosun.di.trans.steps.dubboclient;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.gosun.di.ui.trans.steps.dubboclient.utils.DynamicClassLoader;

@Step( id = "DubboClient", image = "DubboClient.png",
i18nPackageName = "com.gosun.di.trans.steps.dubboclient", name = "DubboClient.TransName",
description = "DubboClient.TransDescription",
categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Lookup" )
public class DubboClientMeta extends BaseStepMeta implements StepMetaInterface {
	
	public static Map<String, String[]> dubboPrividerServices; // 存放所有dubbo提供者的服务
	public static DynamicClassLoader dynaClassLoader; // 自定义classloader加载dubbo提供者jar
	
	private String appName; // 应用名称
	private String registryAddr; // 注册中心地址,形式为ip:port
	private String regProtocol; // 注册中心所用协议
	private String execProvider; // 服务提供者
	private String execInterface; // 服务调用接口
	private String execMethodName; // 服务调用方法
	
	private Method execMethod; // 调用接口的实际方法
	private Class<?>[] execMethodArgTypes; // 调用方法具体参数信息
	private Class<?> execMethodReturnType; // 调用方法返回值信息
	
	private String[] argValues; // 方法参数值
	
	private List<Method> allMethods; // 存放调用接口的所有可调用方法
	
	static {
		dubboPrividerServices = new HashMap<String, String[]>();
		dynaClassLoader = DynamicClassLoader.getDynamicClassLoader();
		try {
			loadDubboProviderJars();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void loadDubboProviderJars() throws Exception {
		// TODO 这里需要解决如何存放dubbo provider目录的问题,暂时写死
		URL resUrl = DubboClientMeta.class.getClassLoader().getResource("./plugins/gosun-dubboclient-plugin/dubboProvider");
		
		String basePath = resUrl.getFile();
		
		File rootFile = new File(basePath);
		if(rootFile.exists() && rootFile.isDirectory()) {
			File[] subFiles = rootFile.listFiles();
			for(File subFile : subFiles) {
				if(subFile.isDirectory()) {
					loadDubboProviderJar(subFile);
				}
			}
		}
		
	}
	
	// 加载dubbo提供者jar
	private static void loadDubboProviderJar(File jarDir) throws Exception {
		Properties props = new Properties();
		File mainfestFile = new File(jarDir, DubboProviderConst.PROVIDER_DEFITION_FILE_NAME);
		if(mainfestFile.exists() && mainfestFile.isFile()) {
			FileInputStream fin = new FileInputStream(mainfestFile);
			props.load(fin);
			fin.close();
			String providerName = props.getProperty(DubboProviderConst.PROP_PROVIDER_NAME);
			String interfaces = props.getProperty(DubboProviderConst.PROP_PROVIDER_INTERFACES);
			dynaClassLoader.loadJarInDir(jarDir.getPath());
			String libPath = jarDir.getPath() + DubboProviderConst.PROVIDER_LIB_PATH;
			dynaClassLoader.loadJarInDir(libPath);
			String[] serviceClassNames = interfaces.split(",");
			dubboPrividerServices.put(providerName, serviceClassNames);
		}
	}

	@Override
	public String getDialogClassName() {
		// 在这里可以自定义step dialog的全限定类名
		return super.getDialogClassName();
	}

	@Override
	public void setDefault() {
		// do something ???
	}

	@Override
	public StepInterface getStep(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		return new DubboClient(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new DubboClientData();
	}
	
	@Override
	  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
	    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
		
//		参考webservice组件
//	    // Input rows and output rows are different in the webservice step
//	    //
//	    if ( !isPassingInputData() ) {
//	      r.clear();
//	    }
//
//	    // Add the output fields...
//	    //
//	    for ( WebServiceField field : getFieldsOut() ) {
//	      int valueType = field.getType();
//
//	      // If the type is unrecognized we give back the XML as a String...
//	      //
//	      if ( field.getType() == ValueMetaInterface.TYPE_NONE ) {
//	        valueType = ValueMetaInterface.TYPE_STRING;
//	      }
//
//	      try {
//	        ValueMetaInterface vValue = ValueMetaFactory.createValueMeta( field.getName(), valueType );
//	        vValue.setOrigin( name );
//	        r.addValueMeta( vValue );
//	      } catch ( Exception e ) {
//	        throw new KettleStepException( e );
//	      }
//	    }
		
		// 设置dubbo调用返回值
		ValueMetaInterface vValue = null;
		try {
			vValue = ValueMetaFactory.createValueMeta("return", ValueMetaInterface.TYPE_STRING);
			r.addValueMeta(vValue);
		} catch (KettlePluginException e) {
			throw new KettleStepException( e );
		}
		
	  }
	
	
	// 返回DubboClientMeta信息,以xml格式返回
	@Override
	public String getXML() throws KettleException {
		StringBuffer retval = new StringBuffer();

	    // 保存应用配置
	    //
	    retval.append( "    " + XMLHandler.addTagValue( "dcAppName", getAppName() ) );

	    // 保存注册中心配置
	    //
	    retval.append( "    " + XMLHandler.addTagValue( "dcRegistryAddr", getRegistryAddr() ) );
	    retval.append( "    " + XMLHandler.addTagValue( "dcRegProtocol", getRegProtocol() ) );
	    
	    
	    // 保存提供者配置
	    retval.append( "    " + XMLHandler.addTagValue( "dcExecProvider", getExecProvider() ) );
	    retval.append( "    " + XMLHandler.addTagValue( "dcExecInterface", getExecInterface() ) );
	    retval.append( "    " + XMLHandler.addTagValue( "dcExecMethod", getExecMethodName() ) );

//	    // 保存调用方法参数信息
//	    //
	    retval.append( "    <args>" + Const.CR );
	    for ( int i = 0; i < execMethodArgTypes.length; i++ ) {
	      Class<?> argType = execMethodArgTypes[i];
	      String argValue = null;
	      if(argValues.length >= i) {
	    	  argValue = argValues[i];
	      }
	      retval.append( "    <arg>" + Const.CR );
	      retval.append( "        " + XMLHandler.addTagValue( "argName", "arg" + i ) );
	      retval.append( "        " + XMLHandler.addTagValue( "argType", argType.getCanonicalName() ) );
	      retval.append( "        " + XMLHandler.addTagValue( "argValue", argValue ) );
	      retval.append( "    </arg>" + Const.CR );
	    }
	    retval.append( "      </args>" + Const.CR );
	    
//	    // 保存调用方法返回信息
//	    //
	    retval.append( "    <return>" + Const.CR );
	    retval.append( "        " + XMLHandler.addTagValue( "returnType", execMethodReturnType.getCanonicalName() ) );
	    retval.append( "      </return>" + Const.CR );

	    return retval.toString();
	}

	// 从资源库中获取DubboClientMeta信息
	@Override
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step,
			List<DatabaseMeta> databases) throws KettleException {

		// 应用配置
		setAppName(rep.getStepAttributeString(id_step, "dcAppName"));
		
		// 注册中心配置
		setRegistryAddr(rep.getStepAttributeString(id_step, "dcRegistryAddr"));
		setRegProtocol(rep.getStepAttributeString(id_step, "dcRegProtocol"));
		
		// 提供者配置
		setExecProvider(rep.getStepAttributeString(id_step, "dcExecProvider"));
		setExecInterface(rep.getStepAttributeString(id_step, "dcExecInterface"));
		setExecMethodName(rep.getStepAttributeString(id_step, "dcExecMethod"));
		
		// 调用方法参数配置
		int nr = rep.countNrStepAttributes(id_step, "arg_type");
		Class<?>[] methodArgTypes = new Class<?>[nr];
		String[] argValues = new String[nr];
		try {
			for(int i = 0; i < nr; i++) {
				methodArgTypes[i] = dynaClassLoader.loadClass(rep.getStepAttributeString(id_step, i, "arg_type"));
				argValues[i] = rep.getStepAttributeString(id_step, i, "arg_value");
			}
			setExecMethodArgTypes(methodArgTypes);
			setArgValues(argValues);
		} catch(ClassNotFoundException e) {
			throw new KettleException(e);
		}
		
		
		// 调用方法返回值配置
		Class<?> methodReturnType = null;
		try {
			methodReturnType = dynaClassLoader.loadClass(rep.getStepAttributeString(id_step, "return_type"));
			setExecMethodReturnType(methodReturnType);
		} catch(ClassNotFoundException e) {
			throw new KettleException(e);
		}
		
	}

	// 从xml节点中加载DubboClientMeta信息
	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			IMetaStore metaStore) throws KettleXMLException {
		
		// 应用配置
		setAppName(XMLHandler.getTagValue(stepnode, "dcAppName"));
		
		// 注册中心配置
		setRegistryAddr(XMLHandler.getTagValue(stepnode, "dcRegistryAddr"));
		setRegProtocol(XMLHandler.getTagValue(stepnode, "dcRegProtocol"));
		
		// 提供者配置
		setExecProvider(XMLHandler.getTagValue(stepnode, "dcExecProvider"));
		setExecInterface(XMLHandler.getTagValue(stepnode, "dcExecInterface"));
		setExecMethodName(XMLHandler.getTagValue(stepnode, "dcExecMethod"));
		
		// 调用方法参数配置
		Node args = XMLHandler.getSubNode(stepnode, "args");
		int nrArgs = XMLHandler.countNodes(args, "arg");
		
		Class<?>[] methodArgTypes = new Class<?>[nrArgs];
		String[] argValues = new String[nrArgs];
		
		try {
			for(int i = 0; i < nrArgs; i++) {
				Node arg = XMLHandler.getSubNodeByNr(args, "arg", i);
				methodArgTypes[i] = dynaClassLoader.loadClass(XMLHandler.getTagValue(arg, "argType"));
				argValues[i] = XMLHandler.getTagValue(arg, "argValue");
			}
			setExecMethodArgTypes(methodArgTypes);
			setArgValues(argValues);
		} catch(ClassNotFoundException e) {
			throw new KettleXMLException(e);
		}
		
		// 调用方法返回值配置
		Node ret = XMLHandler.getSubNode(stepnode, "return");
		try {
			Class<?> methodReturnType = dynaClassLoader.loadClass(XMLHandler.getTagValue(ret, "returnType"));
			setExecMethodReturnType(methodReturnType);
		} catch (ClassNotFoundException e) {
			throw new KettleXMLException(e);
		}
	}

	// 保存DubboClientMeta信息到资源库中
	@Override
	public void saveRep(Repository rep, IMetaStore metaStore,
			ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		
		// 应用配置
		rep.saveStepAttribute(id_transformation, id_step, "dcAppName", getAppName());
		
		// 注册中心配置
		rep.saveStepAttribute(id_transformation, id_step, "dcRegistryAddr", getRegistryAddr());
		rep.saveStepAttribute(id_transformation, id_step, "dcRegProtocol", getRegProtocol());
		
		// 提供者配置
		rep.saveStepAttribute(id_transformation, id_step, "dcExecProvider", getExecProvider());
		rep.saveStepAttribute(id_transformation, id_step, "dcExecInterface", getExecInterface());
		rep.saveStepAttribute(id_transformation, id_step, "dcExecMethod", getExecMethodName());
		
		
		// 调用方法参数配置
		for(int i = 0; i < execMethodArgTypes.length; i++) {
			Class<?> argType = execMethodArgTypes[i];
			String argValue = argValues[i];
			rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", "arg" + i);
			rep.saveStepAttribute(id_transformation, id_step, i, "arg_type", argType.getCanonicalName());
			rep.saveStepAttribute(id_transformation, id_step, i, "arg_value", argValue);
		}
		
		// 调用方法返回值配置
		rep.saveStepAttribute(id_transformation, id_step, "return_type", execMethodReturnType.getCanonicalName());
		
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getRegistryAddr() {
		return registryAddr;
	}

	public void setRegistryAddr(String registryAddr) {
		this.registryAddr = registryAddr;
	}

	public String getRegProtocol() {
		return regProtocol;
	}

	public void setRegProtocol(String regProtocol) {
		this.regProtocol = regProtocol;
	}

	public String getExecProvider() {
		return execProvider;
	}

	public void setExecProvider(String execProvider) {
		this.execProvider = execProvider;
	}

	public String getExecInterface() {
		return execInterface;
	}

	public void setExecInterface(String execInterface) {
		this.execInterface = execInterface;
	}

	public String getExecMethodName() {
		return execMethodName;
	}

	public void setExecMethodName(String execMethodName) {
		this.execMethodName = execMethodName;
	}

	public List<Method> getAllMethods() {
		return allMethods;
	}

	public void setAllMethods(List<Method> allMethods) {
		this.allMethods = allMethods;
	}

	public Class<?>[] getExecMethodArgTypes() {
		return execMethodArgTypes;
	}

	public void setExecMethodArgTypes(Class<?>[] execMethodArgTypes) {
		this.execMethodArgTypes = execMethodArgTypes;
	}

	public Class<?> getExecMethodReturnType() {
		return execMethodReturnType;
	}

	public void setExecMethodReturnType(Class<?> execMethodReturnType) {
		this.execMethodReturnType = execMethodReturnType;
	}

	public Method getExecMethod() {
		return execMethod;
	}

	public void setExecMethod(Method execMethod) {
		this.execMethod = execMethod;
	}

	public String[] getArgValues() {
		return argValues;
	}

	public void setArgValues(String[] argValues) {
		this.argValues = argValues;
	}
	

}
