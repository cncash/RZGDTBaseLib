package com.runzhong.technology.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import java.util.Iterator;
import java.util.Map;

/**
 *************************
 * 本地xml信息工具类 
 * @author cn
 *************************
 */
public class RZXmlUtil {
	private SharedPreferences sp;

	/************************
	 * 初始化SharedPreferences
	 * 
	 * @param fileName
	 * 
	 ************************/
	public RZXmlUtil(Context context, String fileName) {
		// 获取SharedPreferences对象
		sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
	}

	/*************************
	 * 将数据保存到 SharedPreferences中
	 * 
	 * @param input
	 *************************/
	public void save(Map<String, String> input) {
		Editor editor = sp.edit();
		if (!input.isEmpty()) {
			Iterator iter = input.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = entry.getKey().toString();
				String val = entry.getValue().toString();

				editor.putString(key, val);
			}
			editor.commit();
		}

	}

	/*************************
	 * 将数据保存到 SharedPreferences中
	 * 
	 * @param input
	 *************************/
	public void saveObject(Map<String, Object> input) {
		Editor editor = sp.edit();
		if (!input.isEmpty()) {
			Iterator iter = input.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = entry.getKey().toString();
				String val = entry.getValue().toString();

				editor.putString(key, val);
			}
			editor.commit();
		}

	}

	/**
	 ************************* 
	 * 保存数据
	 * 
	 * @param key
	 * @param value
	 ************************* 
	 */
	public void put(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public void put(String key, int value) {
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public void put(String key, boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/*************************
	 * 从 SharedPreferences中 读取所有数据
	 * 
	 * @return
	 *************************/
	public Map<String, ?> load() {
		return sp.getAll();
	}

	/*************************
	 * 清除 SharedPreferences 中所有数据
	 *************************/
	public void clear() {
		sp.edit().clear().commit();
	}

	/*************************
	 * 根据key 删除
	 * 
	 * @param key
	 *************************/
	public void remove(String key) {
		sp.edit().remove(key).commit();
	}

	/*************************
	 * 根据key获取 对应的值
	 * 如果获取不到返回默认值defaultValue
	 * @param key
	 * @return
	 *************************/
	public String getString(String key, String defaultValue) {
		return sp.getString(key, defaultValue);
	}

	/*************************
	 * 根据key获取 对应的值
	 * 如果获取不到返回空字符串
	 * @param key
	 * @return
	 *************************/
	public String getString(String key) {
		return sp.getString(key, "");
	}

	public boolean getBoolean(String key){
		return sp.getBoolean(key,false);
	}

	public boolean getBoolean(String key,boolean defaultValue){
		return sp.getBoolean(key,defaultValue);
	}
	public int getInt(String key){
		return sp.getInt(key,0);
	}
	public int getInt(String key,int defaultValue){
		return sp.getInt(key,defaultValue);
	}
	public float getFloat(String key , float defaultValue){
		return  sp.getFloat(key,defaultValue);
	}

}
