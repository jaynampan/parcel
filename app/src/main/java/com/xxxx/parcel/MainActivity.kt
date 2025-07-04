package com.xxxx.parcel

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xxxx.parcel.ui.AboutScreen
import com.xxxx.parcel.ui.AddRuleScreen
import com.xxxx.parcel.ui.FailSmsScreen
import com.xxxx.parcel.ui.HomeScreen
import com.xxxx.parcel.ui.RulesScreen
import com.xxxx.parcel.ui.SuccessSmsScreen
import com.xxxx.parcel.ui.theme.ParcelTheme
import com.xxxx.parcel.util.PermissionUtil
import com.xxxx.parcel.util.SmsParser
import com.xxxx.parcel.util.SmsUtil
import com.xxxx.parcel.util.getAllCustomPatterns
import com.xxxx.parcel.viewmodel.ParcelViewModel
import com.xxxx.parcel.widget.ParcelWidget
import androidx.core.net.toUri

val smsParser = SmsParser()
var viewModel = ParcelViewModel(smsParser)

class MainActivity : ComponentActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private lateinit var smsContentObserver: ContentObserver
    private lateinit var appDetailsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    val context= this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 注册 ActivityResultLauncher
        appDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            init()
            setContent {
                App(context, guideToSettings={ guideToSettings() }, readAndParseSms = {readAndParseSms()})
            }
        }

        getAllCustomPatterns(this, viewModel)
        init()

        setContent {
            App(context, guideToSettings={ guideToSettings() }, readAndParseSms = {readAndParseSms()})
        }
    }


    private fun startSmsDeletionMonitoring() {
        smsContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                readAndParseSms()
            }
        }
        contentResolver.registerContentObserver(
            "content://sms".toUri(),
            true,
            smsContentObserver
        )
    }

    fun readAndParseSms() {
        val context = applicationContext
        val smsList = SmsUtil.readAllSms(context)
        viewModel.clearData()
        smsList.forEach { sms ->
            viewModel.handleReceivedSms(sms)
        }
        // 刷新 AppWidget（不传递 appWidgetId 以更新所有实例）
        ParcelWidget.updateAppWidget(
            context,
            AppWidgetManager.getInstance(context),
            null,
            viewModel
        )


    }

    private  fun init(){
        // 检查并请求短信权限
        if (!PermissionUtil.hasSmsPermissions(this)) {
             ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                ),
                1
            )
            if (PermissionUtil.hasSmsPermissions(this)) {
                readAndParseSms()
                startSmsDeletionMonitoring()
            }else{
                Toast.makeText(this, "短信权限被拒绝", Toast.LENGTH_SHORT).show()
            }

        } else {
            // 权限已授予，读取短信
            readAndParseSms()
            startSmsDeletionMonitoring()
        }
        setContent {
            App(context, guideToSettings={ guideToSettings() }, readAndParseSms = {readAndParseSms()})
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   init()

                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {
                        init()
                    } else {
                        guideToSettings()
                    }
                }
            }
        }
    }

    private fun guideToSettings() {
        val uri = Uri.fromParts("package", "com.xxxx.parcel", null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = uri
        }
        appDetailsLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsContentObserver)
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Context,guideToSettings:()->Unit,readAndParseSms:()->Unit){
    ParcelTheme {
        val navController = rememberNavController()
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(context,viewModel, navController,onCallBack = {guideToSettings()})
                }
                composable(
                    route = "add_rule?message={message}",
                    arguments = listOf(
                        navArgument("message") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val message = backStackEntry.arguments?.getString("message") ?: ""
                    AddRuleScreen(context,viewModel, navController,message,onCallback = {readAndParseSms()})
                }
                composable("rules") {
                    RulesScreen(context,viewModel, navController,onCallback = {readAndParseSms()})
                }
                composable("fail_sms") {
                    FailSmsScreen(viewModel, navController)
                }
                composable("success_sms") {
                    SuccessSmsScreen(viewModel,  navController)
                }
                composable("about") {
                    AboutScreen(navController)
                }
            }
        }
    }
}