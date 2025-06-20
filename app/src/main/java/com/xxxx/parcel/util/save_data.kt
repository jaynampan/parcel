package com.xxxx.parcel.util

import android.content.Context
import android.content.SharedPreferences
import com.xxxx.parcel.viewmodel.ParcelViewModel

// 保存字符串列表到 SharedPreferences
fun saveCustomPatterns(context: Context, key: String, stringSet: Set<String>) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putStringSet(key, stringSet)
    editor.apply()
}

// 从 SharedPreferences 读取字符串列表
fun getCustomPatterns(context: Context, key: String): MutableSet<String> {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
}

// 添加单个字符串到存储的字符串列表
fun addCustomPatterns(context: Context, key: String, newString: String) {
    // 读取已有的字符串集合
    val existingSet = getCustomPatterns(context, key)
    // 添加新的字符串
    existingSet.add(newString)
    // 保存更新后的集合
    saveCustomPatterns(context, key, existingSet)
}


fun getAllCustomPatterns(context: Context, viewModel: ParcelViewModel) {
    val listAddr = getCustomPatterns(context, "address").toMutableList()
    val listCode = getCustomPatterns(context, "code").toMutableList()
    listAddr.forEach {
        viewModel.addCustomAddressPattern(it)
    }
    listCode.forEach {
        viewModel.addCustomCodePattern(it)
    }
}


fun clearAllCustomPatternsa(context: Context, viewModel: ParcelViewModel) {
    // 获取 SharedPreferences 实例
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    // 获取 Editor 对象
    val editor = sharedPreferences.edit()
    // 清除所有数据
    editor.clear()
    // 异步提交更改
    editor.apply()
    viewModel.clearAllCustomPatterns()
}