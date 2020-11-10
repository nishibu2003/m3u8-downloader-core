/**
 * 参考：http://gootara.org/library/2014/04/javaapijsonjdk1618.html
 * 
 * 
 */

package com.nishibu2003.m3u8_downloader.util;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
    
public class JSONParser {
    
    public static Map<String, Object> jsonToMap(String script) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        // ScriptEngine の eval に JSON を渡す時は、括弧で囲まないと例外が発生します。eval はセキュリティ的には好ましくないので、安全であることが不明なデータを扱うことは想定していません。
        // 外部ネットワークと連携するプログラムで使用しないでください。
        Object obj = engine.eval(String.format("(%s)", script));
        // Rhino は、jdk1.6,7までの JavaScript エンジン。jdk1.8は「jdk.nashorn.api.scripting.NashornScriptEngine」
        Map<String, Object> map = jsonToMap(obj, engine.getClass().getName().equals("com.sun.script.javascript.RhinoScriptEngine"));
        return map;
    }
    
    @SuppressWarnings("rawtypes")
	public static Map<String, Object> jsonToMap(Object obj, boolean rhino) throws Exception {
        // Nashorn の場合は isArray で obj が配列かどうか判断できますが、特に何もしなくても配列番号をキーにして値を取得し Map に格納できるので、ここでは無視しています。
        // Rhino だとインデックスを文字列として指定した場合に値が返ってこないようなので、仕方なく処理を切り分けました。
        // 実際は HashMap なんか使わずに自分で定義したクラス（配列はそのオブジェクトの List プロパティ）にマップすることになると思うので、動作サンプルとしてはこんなもんでよろしいかと。
        boolean array = rhino ? Class.forName("sun.org.mozilla.javascript.internal.NativeArray").isInstance(obj) : false;
        Class scriptObjectClass = Class.forName(rhino ? "sun.org.mozilla.javascript.internal.Scriptable" : "jdk.nashorn.api.scripting.ScriptObjectMirror");
        // キーセットを取得
        Object[] keys = rhino ? (Object[])obj.getClass().getMethod("getIds").invoke(obj) : ((java.util.Set)obj.getClass().getMethod("keySet").invoke(obj)).toArray();
        // get メソッドを取得
        Method method_get = array ? obj.getClass().getMethod("get", int.class, scriptObjectClass) : (rhino ? obj.getClass().getMethod("get", Class.forName("java.lang.String"), scriptObjectClass) : obj.getClass().getMethod("get", Class.forName("java.lang.Object")));
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Object key : keys) {
            Object val = array ? method_get.invoke(obj, (Integer)key, null) : (rhino ? method_get.invoke(obj, key.toString(), null) : method_get.invoke(obj, key));
            if (scriptObjectClass.isInstance(val)) {
                map.put(key.toString(), jsonToMap(val, rhino));
            } else {
                map.put(key.toString(), val == null ? null : val.toString()); // サンプルなので、ここでは単純に toString() してますが、実際は val の型を有効に活用した方が良いでしょう。
            }
        }
        return map;
    }

    public static <T> T getValue(Map<?, ?> map, String key, Class<T> className) throws Exception {
        if (key == null || key.isEmpty()) {
            throw new Exception("key is required.");
        }

        int pos = key.indexOf(".");
        if (pos == -1) {
            return (T)map.get(key);
        } else {
            String key1 = key.substring(0, pos);
            String key2 = key.substring(pos + 1);
            Object obj = map.get(key1);
            if (obj instanceof Map) {
                return getValue((Map)obj, key2, className);
            }
            throw new ClassCastException(obj.getClass().getName() + " is not an instanceof Map");
        }
    }

}