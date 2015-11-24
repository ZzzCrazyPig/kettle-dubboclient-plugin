package com.gosun.di.trans.steps.dubboclient;

import java.lang.reflect.Method;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.google.gson.Gson;
import com.gosun.di.ui.trans.steps.dubboclient.utils.DynamicClassLoader;
import com.gosun.di.ui.trans.steps.dubboclient.utils.ReflectionUtil;

public class DubboClient extends BaseStep implements StepInterface {
	
	private static Class<?> PKG = DubboClientMeta.class; // for i18n purposes, needed by Translator2!!
	
	private DubboClientMeta meta;
	private DubboClientData data;
	
	private static Gson gson = new Gson();

	public DubboClient(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (DubboClientMeta) smi;
		data = (DubboClientData) sdi;
		
		
		data.outputRowMeta = new RowMeta();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);
		
		String appName = meta.getAppName();
		String registryAddr = meta.getRegistryAddr();
		String regProtocol = meta.getRegProtocol();
		String execInterface = meta.getExecInterface();
		String execMethodName = meta.getExecMethodName();
		Class<?>[] execMethodArgTypes = meta.getExecMethodArgTypes();
		Class<?> execMethodReturnType = meta.getExecMethodReturnType();
		
		String[] argValues = meta.getArgValues();
		
		// 检查参数
		if(Const.isEmpty(appName)) {
			throw new KettleException(BaseMessages.getString(PKG, "DubboClient.Error.AppNameEmpty"));
		}
		if(Const.isEmpty(registryAddr)) {
			throw new KettleException(BaseMessages.getString(PKG, "DubboClient.Error.RegistryAddrEmpty"));
		}
		if(Const.isEmpty(regProtocol)) {
			throw new KettleException(BaseMessages.getString(PKG, "DubboClient.Error.RegProtocolEmpty"));
		}
		if(Const.isEmpty(execInterface)) {
			throw new KettleException(BaseMessages.getString(PKG, "DubboClient.Error.ExecInterfaceEmpty"));
		}
		if(Const.isEmpty(execMethodName)) {
			throw new KettleException(BaseMessages.getString(PKG, "DubboClient.Error.ExecMethodEmpty"));
		}
		// ....
		
		// 使用dubbo client api 请求dubbo服务
		DynamicClassLoader dynaClassLoader = DubboClientMeta.dynaClassLoader;
		Class<?> serviceClazz = null;
		try {
			serviceClazz = dynaClassLoader.loadClass(execInterface);
			// 配置当前应用
			ApplicationConfig app = new ApplicationConfig();
			app.setName(appName);

			// 连接注册中心配置
			RegistryConfig registry = new RegistryConfig();
			registry.setProtocol(regProtocol); // 协议,一定要配置
			registry.setAddress(registryAddr);

			// 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接

			// 引用远程服务
			// 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
			ReferenceConfig reference = new ReferenceConfig();
			reference.setApplication(app);
			reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
			reference.setInterface(serviceClazz);

			Object service = reference.get();
			
			// 得到具体的method
			Method execMethod = meta.getExecMethod();
			if(execMethod == null) {
				execMethod = ReflectionUtil.getMethod(service.getClass(), execMethodName, execMethodArgTypes);
				meta.setExecMethod(execMethod);
			}
			
			Object[] realArgValues = null;
			
			// TODO 参数类型变换
			// 对于复杂数据类型,传递过来的是json字符串,需要还原为实际的对象
			if(execMethodArgTypes != null) {
				realArgValues = new Object[execMethodArgTypes.length];
				for(int i = 0; i < execMethodArgTypes.length; i++) {
					Class<?> argType = execMethodArgTypes[i];
					String className = argType.getCanonicalName();
					if(!className.startsWith("java.lang")) {
						realArgValues[i] = gson.fromJson(argValues[i], argType);
					}
				}
			}
			
			// 通过反射来调用
			Object result = ReflectionUtil.invoke(execMethod, service, realArgValues);
			reference.destroy(); // 是否是必须的?
			// 处理返回结果
			processOutput(result, execMethodReturnType);
			// 结束状态设置
			setOutputDone();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new KettleException(e);
		}
		
		return false;
	}
	
	private void processOutput(Object result, Class<?> methodReturnType) throws KettleStepException {
		// 将结果传递到下一个步骤
		putRow(data.outputRowMeta, new Object[]{result});
	}
	
	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		return super.init(smi, sdi);
	}

	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
	}


}
