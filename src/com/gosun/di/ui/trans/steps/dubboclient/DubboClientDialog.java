package com.gosun.di.ui.trans.steps.dubboclient;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.gosun.di.trans.steps.dubboclient.DubboClientMeta;
import com.gosun.di.ui.trans.steps.dubboclient.utils.ReflectionUtil;

public class DubboClientDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = DubboClientMeta.class; // for i18n purposes, needed by Translator2!!
	
	private DubboClientMeta meta;
	
	private CTabFolder wTabFolder;
	
	private CTabItem tabItemDubboClient;
	private CTabItem tabItemArgs;
	
	private Label wlAppName;
	private TextVar wAppName;
	
	private Label wlRegistryAddr;
	private TextVar wRegistryAddr;
	
	private Label wlRegProtocol;
	private CCombo wRegProtocol;
	
	private Label wlExecProvider;
	private CCombo wExecProvider;
	
	private Label wlExecInterface;
	private CCombo wExecInterface;
	
	private Label wlExecMethod;
	private CCombo wExecMethod;
	
	private TableView argsTableView;
	
	private ModifyListener lsMod = new ModifyListener() {
	    public void modifyText( ModifyEvent e ) {
	      meta.setChanged();
	    }
	  };
	
	public DubboClientDialog(Shell parent, Object in,
			TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta) in, transMeta, stepname);
		this.meta = (DubboClientMeta) in;
	}

	@Override
	public String open() {
		Shell parent = getParent();
	    Display display = parent.getDisplay();

	    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
	    props.setLook( shell );
	    setShellImage( shell, meta ); // 设置dialog icon图标
	    
	    changed = meta.hasChanged();

	    // 设置界面主布局
	    FormLayout formLayout = new FormLayout();
	    formLayout.marginWidth = Const.FORM_MARGIN;
	    formLayout.marginHeight = Const.FORM_MARGIN;

	    shell.setLayout( formLayout );
	    shell.setText( BaseMessages.getString( PKG, "DubboClientDialog.DialogTitle" ) );

	    int middle = props.getMiddlePct();
	    int margin = Const.MARGIN;
	    
	    // 步骤名称设置
	    wlStepname = new Label( shell, SWT.RIGHT );
	    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
	    props.setLook( wlStepname );
	    fdlStepname = new FormData();
	    fdlStepname.left = new FormAttachment( 0, 0 );
	    fdlStepname.top = new FormAttachment( 0, margin );
	    fdlStepname.right = new FormAttachment( middle, -margin );
	    wlStepname.setLayoutData( fdlStepname );
	    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wStepname.setText( stepname );
	    props.setLook( wStepname );
	    wStepname.addModifyListener( lsMod );
	    fdStepname = new FormData();
	    fdStepname.left = new FormAttachment( middle, 0 );
	    fdStepname.top = new FormAttachment( 0, margin );
	    fdStepname.right = new FormAttachment( 100, 0 );
	    wStepname.setLayoutData( fdStepname );

	    // 配置tab
	    wTabFolder = new CTabFolder( shell, SWT.BORDER );
	    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
	    
	    tabItemDubboClient = new CTabItem( wTabFolder, SWT.NONE );
	    tabItemDubboClient.setText( BaseMessages.getString( PKG, "DubboClientDialog.MainTab.TabTitle" ) );
	    Composite compositeTabDubboClient = new Composite( wTabFolder, SWT.NONE );
	    props.setLook( compositeTabDubboClient );

	    FormLayout fileLayout = new FormLayout();
	    fileLayout.marginWidth = 3;
	    fileLayout.marginHeight = 3;
	    compositeTabDubboClient.setLayout( fileLayout );
	    
	    // 新建field set(应用配置)
	    
	    Group gApp = new Group( compositeTabDubboClient, SWT.SHADOW_ETCHED_IN );
	    gApp.setText( BaseMessages.getString( PKG, "DubboClientDialog.ApplicationConfig.Label" ) );
	    FormLayout appLayout = new FormLayout();
	    appLayout.marginWidth = 3;
	    appLayout.marginHeight = 3;
	    gApp.setLayout( appLayout );
	    props.setLook( gApp );
	    
	    wlAppName = new Label(gApp, SWT.RIGHT);
	    wlAppName.setText( BaseMessages.getString( PKG, "DubboClientDialog.AppName.Label" ) );
	    props.setLook( wlAppName );
	    FormData fdlAppName = new FormData();
	    fdlAppName.top = new FormAttachment( 0, margin );
	    fdlAppName.left = new FormAttachment( 0, 0 );
	    fdlAppName.right = new FormAttachment( middle, -margin );
	    wlAppName.setLayoutData( fdlAppName );
	    wAppName = new TextVar( transMeta, gApp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wAppName.addModifyListener( lsMod );
	    wAppName.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.AppName.Tooltip" ) );
	    props.setLook( wAppName );
	    FormData fdAppName = new FormData();
	    fdAppName.top = new FormAttachment( 0, margin );
	    fdAppName.left = new FormAttachment( middle, 0 );
	    fdAppName.right = new FormAttachment( 100, 0 );
	    wAppName.setLayoutData( fdAppName );
	    
	    FormData fdApp = new FormData();
	    fdApp.left = new FormAttachment( 0, 0 );
	    fdApp.right = new FormAttachment( 100, 0 );
	    fdApp.top = new FormAttachment( 0, margin );
	    gApp.setLayoutData( fdApp );
	    
	    
	    // 新建field set(注册中心配置)
	    Group gRegistry = new Group( compositeTabDubboClient, SWT.SHADOW_ETCHED_IN );
	    gRegistry.setText( BaseMessages.getString( PKG, "DubboClientDialog.RegistryConfig.Label" ) );
	    FormLayout registryLayout = new FormLayout();
	    registryLayout.marginWidth = 3;
	    registryLayout.marginHeight = 3;
	    gRegistry.setLayout( registryLayout );
	    props.setLook( gRegistry );
	    
	    // 注册中心地址
	    wlRegistryAddr = new Label(gRegistry, SWT.RIGHT);
	    wlRegistryAddr.setText( BaseMessages.getString( PKG, "DubboClientDialog.RegistryAddr.Label" ) );
	    props.setLook( wlRegistryAddr );
	    FormData fdlRegistryAddr = new FormData();
	    fdlRegistryAddr.top = new FormAttachment( 0, margin );
	    fdlRegistryAddr.left = new FormAttachment( 0, 0 );
	    fdlRegistryAddr.right = new FormAttachment( middle, -margin );
	    wlRegistryAddr.setLayoutData( fdlRegistryAddr );
	    wRegistryAddr = new TextVar( transMeta, gRegistry, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wRegistryAddr.addModifyListener( lsMod );
	    wRegistryAddr.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.RegistryAddr.Tooltip" ) );
	    props.setLook( wRegistryAddr );
	    FormData fdRegistryAddr = new FormData();
	    fdRegistryAddr.top = new FormAttachment( 0, margin );
	    fdRegistryAddr.left = new FormAttachment( middle, 0 );
	    fdRegistryAddr.right = new FormAttachment( 100, 0 );
	    wRegistryAddr.setLayoutData( fdRegistryAddr );
	    
	    // 注册中心所用协议
	    wlRegProtocol = new Label(gRegistry, SWT.RIGHT);
	    wlRegProtocol.setText( BaseMessages.getString( PKG, "DubboClientDialog.RegProtocol.Label" ) );
	    props.setLook( wlRegProtocol );
	    FormData fdlRegProtocol = new FormData();
	    fdlRegProtocol.top = new FormAttachment( wRegistryAddr, margin );
	    fdlRegProtocol.left = new FormAttachment( 0, 0 );
	    fdlRegProtocol.right = new FormAttachment( middle, -margin );
	    wlRegProtocol.setLayoutData( fdlRegProtocol );
	    wRegProtocol = new CCombo( gRegistry, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wRegProtocol.addModifyListener( lsMod );
	    wRegProtocol.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.RegProtocol.Tooltip" ) );
	    props.setLook( wRegProtocol );
	    FormData fdRegProcotol = new FormData();
	    fdRegProcotol.top = new FormAttachment( wRegistryAddr, margin );
	    fdRegProcotol.left = new FormAttachment( middle, 0 );
	    fdRegProcotol.right = new FormAttachment( 100, 0 );
	    wRegProtocol.setLayoutData( fdRegProcotol );
	    
	    wRegProtocol.setItems(new String[]{
	    		"zookeeper"
	    });
	    
	    FormData fdRegistry = new FormData();
	    fdRegistry.left = new FormAttachment( 0, 0 );
	    fdRegistry.right = new FormAttachment( 100, 0 );
	    fdRegistry.top = new FormAttachment( gApp, margin );
	    gRegistry.setLayoutData( fdRegistry );
	    
	    
	    // 新建field set(调用)
	    Group gExecute = new Group( compositeTabDubboClient, SWT.SHADOW_ETCHED_IN );
	    gExecute.setText( BaseMessages.getString( PKG, "DubboClientDialog.ExecuteConfig.Label" ) );
	    FormLayout execLayout = new FormLayout();
	    execLayout.marginWidth = 3;
	    execLayout.marginHeight = 3;
	    gExecute.setLayout( execLayout );
	    props.setLook( gExecute );
	    
	    // 提供者
	    wlExecProvider = new Label(gExecute, SWT.RIGHT);
	    wlExecProvider.setText(BaseMessages.getString(PKG, "DubboClientDialog.ExecProvider.Label"));
	    props.setLook(wlExecProvider);
	    FormData fdlExecProvider = new FormData();
	    fdlExecProvider.top = new FormAttachment( 0, margin );
	    fdlExecProvider.left = new FormAttachment( 0, 0 );
	    fdlExecProvider.right = new FormAttachment( middle, -margin );
	    wlExecProvider.setLayoutData(fdlExecProvider);
	    wExecProvider = new CCombo(gExecute, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
	    wExecProvider.addModifyListener(lsMod);
	    wExecProvider.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.ExecProvider.Tooltip" ) );
	    props.setLook( wExecProvider );
	    FormData fdExecProvider = new FormData();
	    fdExecProvider.top = new FormAttachment( 0, margin );
	    fdExecProvider.left = new FormAttachment( middle, 0 );
	    fdExecProvider.right = new FormAttachment( 100, 0 );
	    wExecProvider.setLayoutData( fdExecProvider );
	    
	    
	    wExecProvider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onProviderSelected(e);
			}
	    	
		});
	    
	    
	    
	    // 调用接口
	    wlExecInterface = new Label(gExecute, SWT.RIGHT);
	    wlExecInterface.setText( BaseMessages.getString( PKG, "DubboClientDialog.ExecInterface.Label" ) );
	    props.setLook( wlExecInterface );
	    FormData fdlExecInterface = new FormData();
	    fdlExecInterface.top = new FormAttachment( wExecProvider, margin );
	    fdlExecInterface.left = new FormAttachment( 0, 0 );
	    fdlExecInterface.right = new FormAttachment( middle, -margin );
	    wlExecInterface.setLayoutData( fdlExecInterface );
	    wExecInterface = new CCombo( gExecute, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wExecInterface.addModifyListener( lsMod );
	    wExecInterface.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.ExecInterface.Tooltip" ) );
	    props.setLook( wExecInterface );
	    FormData fdExecInterface = new FormData();
	    fdExecInterface.top = new FormAttachment( wExecProvider, margin );
	    fdExecInterface.left = new FormAttachment( middle, 0 );
	    fdExecInterface.right = new FormAttachment( 100, 0 );
	    wExecInterface.setLayoutData( fdExecInterface );
	    
	    wExecInterface.addSelectionListener(new SelectionAdapter() {
	    	
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		try {
					onInterfaceSelected(e);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
	    	}
	    	
		});
	    
	    
	    // 调用方法
	    wlExecMethod = new Label(gExecute, SWT.RIGHT);
	    wlExecMethod.setText( BaseMessages.getString( PKG, "DubboClientDialog.ExecMethod.Label" ) );
	    props.setLook( wlExecMethod );
	    FormData fdlExecMethod = new FormData();
	    fdlExecMethod.top = new FormAttachment( wExecInterface, margin );
	    fdlExecMethod.left = new FormAttachment( 0, 0 );
	    fdlExecMethod.right = new FormAttachment( middle, -margin );
	    wlExecMethod.setLayoutData( fdlExecMethod );
	    wExecMethod = new CCombo( gExecute, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
	    wExecMethod.addModifyListener( lsMod );
	    wExecMethod.setToolTipText( BaseMessages.getString( PKG, "DubboClientDialog.ExecMethod.Tooltip" ) );
	    props.setLook( wExecMethod );
	    FormData fdExecMethod = new FormData();
	    fdExecMethod.top = new FormAttachment( wExecInterface, margin );
	    fdExecMethod.left = new FormAttachment( middle, 0 );
	    fdExecMethod.right = new FormAttachment( 100, 0 );
	    wExecMethod.setLayoutData( fdExecMethod );
	    
	    wExecMethod.addSelectionListener(new SelectionAdapter() {
			
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		onMethodSelected(e);
	    	}
	    	
	    });
	    
	    FormData fdExecute = new FormData();
	    fdExecute.left = new FormAttachment( 0, 0 );
	    fdExecute.right = new FormAttachment( 100, 0 );
	    fdExecute.top = new FormAttachment( gRegistry, margin );
	    gExecute.setLayoutData( fdExecute );
	    
	    // Layout du tab
	    FormData fdFileComp = new FormData();
	    fdFileComp.left = new FormAttachment( 0, 0 );
	    fdFileComp.top = new FormAttachment( 0, 0 );
	    fdFileComp.right = new FormAttachment( 100, 0 );
	    fdFileComp.bottom = new FormAttachment( 100, 0 );
	    compositeTabDubboClient.setLayoutData( fdFileComp );

	    compositeTabDubboClient.layout();
	    tabItemDubboClient.setControl( compositeTabDubboClient );
	    
	    
	    wTabFolder.setSelection( tabItemDubboClient );
	    
	    
	    FormData fdTabFolder = new FormData();
	    fdTabFolder.left = new FormAttachment( 0, 0 );
	    fdTabFolder.top = new FormAttachment( wStepname, margin );
	    fdTabFolder.right = new FormAttachment( 100, 0 );
	    fdTabFolder.bottom = new FormAttachment( 100, -50 );
	    wTabFolder.setLayoutData( fdTabFolder );
	    
	    
	    // Boutons OK / Cancel

	    wOK = new Button( shell, SWT.PUSH );
	    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

	    wCancel = new Button( shell, SWT.PUSH );
	    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

	    setButtonPositions( new Button[] { wOK, /*wAddInput, wAddOutput,*/ wCancel }, margin, wTabFolder );

	    // Detect X or ALT-F4 or something that kills this window...
	    shell.addShellListener( new ShellAdapter() {
	      public void shellClosed( ShellEvent e ) {
	        cancel();
	      }
	    } );

	    wOK.addSelectionListener( new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent e ) {
	        ok();
	      }
	    } );
	    
	    wCancel.addSelectionListener( new SelectionAdapter() {
	      public void widgetSelected( SelectionEvent e ) {
	        cancel();
	      }
	    } );

	    lsDef = new SelectionAdapter() {
	      public void widgetDefaultSelected( SelectionEvent e ) {
	        ok();
	      }
	    };
	    
	    loadCombo();
	    
	    loadDataIntoToUI();
	    
	    if(meta.getExecMethodArgTypes() != null) {
	    	addMethodArgsTab();
	    }
	    
	    setSize();

	    shell.open();

	    while ( !shell.isDisposed() ) {
	      if ( !display.readAndDispatch() ) {
	        display.sleep();
	      }
	    }
	    
		return stepname;
	}
	
	private void ok() {
		if (Const.isEmpty(wStepname.getText())) {
			return;
		}
		stepname = wStepname.getText(); // return value
		saveDataIntoMeta();
		dispose();
	}
	
	private void saveDataIntoMeta() {
		meta.setAppName(wAppName.getText());
		meta.setRegistryAddr(wRegistryAddr.getText());
		meta.setRegProtocol(wRegProtocol.getText());
		meta.setExecProvider(wExecProvider.getText());
		meta.setExecInterface(wExecInterface.getText());
		meta.setExecMethodName(wExecMethod.getText());
		
		TableItem[] tableItems = argsTableView.table.getItems();
		String[] argValues = new String[tableItems.length];
		for(int i = 0; i < tableItems.length; i++) {
			argValues[i] = tableItems[i].getText(3);
		}
		meta.setArgValues(argValues);
	}
	
	private void loadCombo() {
		Map<String, String[]> dubboProviderServices = DubboClientMeta.dubboPrividerServices;
		Set<String> providerSet = dubboProviderServices.keySet();
		TreeSet<String> treeSet = new TreeSet<String>(providerSet);
		String[] providerNames = new String[treeSet.size()];
		treeSet.toArray(providerNames);
		wExecProvider.setItems(providerNames);
	}
	
	private void loadDataIntoToUI() {
		wStepname.setText( stepname );
		wAppName.setText(meta.getAppName() == null ? "" : meta.getAppName());
		wRegistryAddr.setText(meta.getRegistryAddr() == null ? "" : meta.getRegistryAddr());
		wRegProtocol.setText(meta.getRegProtocol() == null ? "" : meta.getRegProtocol());
		wExecProvider.setText(meta.getExecProvider() == null ? "" : meta.getExecProvider());
		wExecInterface.setText(meta.getExecInterface() == null ? "" : meta.getExecInterface());
		wExecMethod.setText(meta.getExecMethodName() == null ? "" : meta.getExecMethodName());
	}
	
	private void cancel() {
		stepname = null;
	    meta.setChanged( changed );
	    dispose();
	}
	
	private void onProviderSelected(SelectionEvent e) {
		CCombo _wExecProvider = (CCombo) e.getSource();
		String selectProvider = _wExecProvider.getText();
		meta.setExecProvider(selectProvider);
		// 加载接口
		String[] interfaceClassNames = DubboClientMeta.dubboPrividerServices.get(selectProvider);
		wExecInterface.setItems(interfaceClassNames);
	}
	
	private void onInterfaceSelected(SelectionEvent e) throws Exception {
		CCombo _wExecInterface = (CCombo) e.getSource();
		String selectInterface = _wExecInterface.getText();
		meta.setExecInterface(selectInterface);
		Class<?> clazz = DubboClientMeta.dynaClassLoader.loadClass(selectInterface);
		// 加载调用方法
		List<Method> methods = ReflectionUtil.getInterfaceMethod(clazz);
		String[] methodNames = new String[methods.size()];
		for(int i = 0; i < methodNames.length; i++) {
			methodNames[i] = methods.get(i).getName();
		}
		wExecMethod.setItems(methodNames);
		
		meta.setAllMethods(methods);
	}
	
	// 选择"调用方法"时候触发的动作
	private void onMethodSelected(SelectionEvent e) {
		CCombo _wExecMethod = (CCombo) e.getSource();
		int selIndex = _wExecMethod.getSelectionIndex();
		Method selMethod = meta.getAllMethods().get(selIndex);
		meta.setExecMethodName(selMethod.getName());
		meta.setExecMethod(selMethod);
		Class<?>[] methodArgTypes = ReflectionUtil.getMethodArgTypes(selMethod);
		Class<?> methodReturnType = ReflectionUtil.getMethodReturnType(selMethod);
		meta.setExecMethodArgTypes(methodArgTypes);
		meta.setExecMethodReturnType(methodReturnType);
		
		
		// 先移除
		removeMethodArgsTab();
		// 再添加
		addMethodArgsTab();
	}
	
	private void removeMethodArgsTab() {
		if(tabItemArgs != null) {
			tabItemArgs.dispose();
			meta.setArgValues(null);
			
			argsTableView = null;
			tabItemArgs = null;
		}
	}
	
	private void addMethodArgsTab() {
		
		TableView oldTableView = argsTableView;
	    int margin = Const.MARGIN;
	    
	    Composite vCompositeTabField = new Composite( wTabFolder, SWT.NONE );
	    FormLayout formLayout = new FormLayout();
	    formLayout.marginWidth = Const.FORM_MARGIN;
	    formLayout.marginHeight = Const.FORM_MARGIN;

	    vCompositeTabField.setLayout( formLayout );
	    props.setLook( vCompositeTabField );

	    if ( tabItemArgs == null ) {
	    	tabItemArgs = new CTabItem( wTabFolder, SWT.NONE );
	    }
	    
	    ColumnInfo colinf1 = new ColumnInfo("argName", ColumnInfo.COLUMN_TYPE_TEXT, false);
	    colinf1.setReadOnly(true);
	    ColumnInfo colinf2 = new ColumnInfo("argType", ColumnInfo.COLUMN_TYPE_TEXT, false);
	    colinf2.setReadOnly(true);
	    ColumnInfo colinf3 = new ColumnInfo("argVal", ColumnInfo.COLUMN_TYPE_TEXT, false);
	    
		ColumnInfo[] colinf = new ColumnInfo[] {
				colinf1,
				colinf2,
				colinf3
		};
	    argsTableView =
	      new TableView( transMeta, vCompositeTabField, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props );
	    argsTableView.setReadonly( false );
	    argsTableView.clearAll();
	    tabItemArgs.setText("args");

	    Button vButton = new Button( vCompositeTabField, SWT.NONE );
	    vButton.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
	    
	    vButton.addSelectionListener(new SelectionAdapter() {
	    	// TODO 
		});

	    Button[] buttons = new Button[] { vButton };
	    BaseStepDialog.positionBottomButtons( vCompositeTabField, buttons, Const.MARGIN, null );

	    FormData fdTable = new FormData();
	    fdTable.left = new FormAttachment( 0, 0 );
	    fdTable.top = new FormAttachment( 0, margin );
	    fdTable.right = new FormAttachment( 100, 0 );
	    fdTable.bottom = new FormAttachment( vButton, 0 );
	    argsTableView.setLayoutData( fdTable );

	    FormData fdInComp = new FormData();
	    fdInComp.left = new FormAttachment( 0, 0 );
	    fdInComp.top = new FormAttachment( 0, 0 );
	    fdInComp.right = new FormAttachment( 100, 0 );
	    fdInComp.bottom = new FormAttachment( 100, 0 );
	    vCompositeTabField.setLayoutData( fdInComp );

	    vCompositeTabField.layout();

	    tabItemArgs.setControl( vCompositeTabField );

	    if(meta.getExecMethodArgTypes() != null) {
	    	for(int i = 0; i < meta.getExecMethodArgTypes().length; i++) {
	    		Class<?> argType = meta.getExecMethodArgTypes()[i];
	    		TableItem vTableItem = new TableItem( argsTableView.table, SWT.NONE );
	    		vTableItem.setText(1, "arg" + i);
	    		vTableItem.setText(2, argType.getCanonicalName());
	    		if(meta.getArgValues() != null) {
	    			vTableItem.setText(3, meta.getArgValues()[i]);
	    		}
	    	}
	    }
	    
	    if ( oldTableView != null ) {
	      oldTableView.dispose();
	    }
	    argsTableView.removeEmptyRows();
	    argsTableView.setRowNums();
	    argsTableView.optWidth( true );
	}
	
}
