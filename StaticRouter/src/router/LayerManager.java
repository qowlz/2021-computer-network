package router;

import java.util.HashMap;

public class LayerManager {

	HashMap<String, BaseLayer> layerMap = new HashMap<String, BaseLayer>();
	
	public void AddLayer(BaseLayer layer){
		layerMap.put(layer.pLayerName, layer);
	}

	public BaseLayer GetLayer(String pName){
		return layerMap.get(pName) ;
	}
	
	public void ConnectLayers(){
		var ni = layerMap.get(Constants.NILayerName);
		var eth = layerMap.get(Constants.EthLayerName);
		var ip = layerMap.get(Constants.IPLayerName);
		var arp = layerMap.get(Constants.ARPLayerName);
		var app = layerMap.get(Constants.AppLayerName);

		ni.SetUpperUnderLayer(eth);
		eth.SetUpperUnderLayer(ip);
		ip.SetUnderLayer(arp);
		arp.SetUnderLayer(eth);
		app.SetUnderLayer(ip);
	}
}