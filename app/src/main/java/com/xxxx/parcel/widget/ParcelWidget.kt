package com.xxxx.parcel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.xxxx.parcel.MainActivity
import com.xxxx.parcel.R
import com.xxxx.parcel.viewmodel.ParcelViewModel

class ParcelWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 获取 ParcelViewModel 实例
        val viewModel = (context.applicationContext as? ViewModelStoreOwner)?.let {
            ViewModelProvider(it)[ParcelViewModel::class.java]
        }

        // 为每个小部件执行更新
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, viewModel)
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        // 当第一个小部件被添加时调用
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        // 当最后一个小部件被移除时调用
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int?,
            viewModel: ParcelViewModel?
        ) {
            // 如果没有提供 appWidgetId，则更新所有实例
            if (appWidgetId == null) {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(
                    android.content.ComponentName(context, ParcelWidget::class.java)
                )
                for (id in ids) {
                    updateSingleAppWidget(context, manager, id, viewModel)
                }
            } else {
                updateSingleAppWidget(context, appWidgetManager, appWidgetId, viewModel)
            }
        }

        private fun updateSingleAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: ParcelViewModel?
        ) {
            // 从 ViewModel 获取最新的取件码信息
            val total = viewModel?.successSmsData?.value?.size ?: 0

            val latestMessage = viewModel?.parcels?.value?.firstOrNull()
            var address1 = latestMessage?.address ?: ""
            var codeList1 = ""
            if (latestMessage != null) {
                codeList1 = latestMessage.codes.joinToString(separator = "\n")
                address1 += "（${latestMessage.codes.size}）"
            } else {
                codeList1 = "无取件码"
            }

            val secondMessage = viewModel?.parcels?.value?.getOrNull(1)
            var address2 = secondMessage?.address ?: ""
            var codeList2 = ""
            if (secondMessage != null) {
                codeList2 = secondMessage.codes.joinToString(separator = "\n")
                address2 += "（${secondMessage.codes.size}）"
            } else {
                codeList2 = "无取件码"
            }
             
            val thirdMessage = viewModel?.parcels?.value?.getOrNull(2)
            var address3 = thirdMessage?.address ?: ""
            var codeList3 = ""
            if (thirdMessage != null) {
                codeList3 = thirdMessage.codes.joinToString(separator = "\n")
                address3 += "（${thirdMessage.codes.size}）"
            } else {
                codeList3 = "无取件码"
            }


            // 构建 RemoteViews 对象
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setTextViewText(R.id.parcel_num, total.toString() )
                setTextViewText(R.id.widget_address1, address1 )
                setTextViewText(R.id.widget_codes1, codeList1)

                setTextViewText(R.id.widget_address2, address2 )
                setTextViewText(R.id.widget_codes2, codeList2)
                setTextViewText(R.id.widget_address3, address3 )
                setTextViewText(R.id.widget_codes3, codeList3)

                // 设置点击意图
                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                setOnClickPendingIntent(R.id.widget_container, PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            }

            // 更新 App Widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}