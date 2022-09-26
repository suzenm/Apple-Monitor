package top.misec.applemonitor.job;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.misec.applemonitor.config.CfgSingleton;
import top.misec.applemonitor.config.MonitorCfg;
import top.misec.applemonitor.push.BarkPush;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Moshi
 */

@Slf4j
public class AppleMonitor {
    private final MonitorCfg CONFIG = CfgSingleton.getInstance().config;
    
    public void monitor() {
        
        //监视机型型号
        List<String> productList = CONFIG.getDeviceCodes();
        
        try {
            for (String k : productList) {
                doMonitor(CONFIG.getLocation(), k);
            }
        } catch (Exception e) {
            log.error("AppleMonitor error", e);
        }
    }
    
    public void doMonitor(String locationName, String productCode) {
        
        Map<String, Object> queryMap = new HashMap<>(10);
        queryMap.put("pl", "true");
        queryMap.put("mts.0", "regular");
        queryMap.put("parts.0", productCode);
        queryMap.put("location", locationName);
        
        String url = "https://www.apple.com.cn/shop/fulfillment-messages?" + URLUtil.buildQuery(queryMap, CharsetUtil.CHARSET_UTF_8);
        
        
        try {
            
            HttpResponse httpResponse = HttpRequest.get(url).execute();
            if (!httpResponse.isOk()) {
                log.info("正在持续监控中...");
                return;
            }
            
            JSONObject jsonObject = JSONObject.parseObject(httpResponse.body());
            
            if ("200".equals(jsonObject.getJSONObject("head").get("status"))) {
                JSONObject pickupMessage = jsonObject.getJSONObject("body")
                        .getJSONObject("content")
                        .getJSONObject("pickupMessage");
                
                JSONArray stores = pickupMessage.getJSONArray("stores");
                
                stores.stream().filter(store -> {
                    JSONObject storeJson = (JSONObject) store;
                    String storeName = storeJson.getString("storeName");
                    
                    if (CONFIG.getStoreWhiteList().isEmpty()) {
                        return true;
                    }
                    return CONFIG.getStoreWhiteList().stream().anyMatch(k -> storeName.contains(k) || k.contains(storeName));
                    
                }).forEach(k -> {
                    
                    JSONObject storeJson = (JSONObject) k;
                    
                    JSONObject partsAvailability = storeJson.getJSONObject("partsAvailability");
                    
                    String storeNames = storeJson.getString("storeName").trim();
                    String status = partsAvailability.getJSONObject(productCode).getString("pickupDisplay");
                    
                    String deviceName = partsAvailability.getJSONObject(productCode).getJSONObject("messageTypes")
                            .getJSONObject("regular")
                            .getString("storePickupProductTitle");
                    
                    String content = storeNames + deviceName + partsAvailability.getJSONObject(productCode).getString("pickupSearchQuote");
                    
                    if ("available".equals(status)) {
                        BarkPush.push(content, CONFIG.getBarkPushUrl(), CONFIG.barkPushToken);
                    }
                    log.info(content);
                    
                });
            }
        } catch (Exception e) {
            log.error("AppleMonitor error", e);
        }
        
    }
}