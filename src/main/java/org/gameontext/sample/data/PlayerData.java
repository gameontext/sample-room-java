package org.gameontext.sample.data;

public class PlayerData {
    private String _id;
    private String _rev;
    private String shoeName = null;
    private String shoeDesc = null;

    public PlayerData() {
      shoeName = "";
      shoeDesc = "";
    }
    
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String get_rev() {	return _rev;}
    public void set_rev(String _rev) { this._rev = _rev; }

    public String getShoeName() { return shoeName; }
    public void setShoeName(String shoeName) { this.shoeName = shoeName; }

    public String getShoeDesc() { return shoeDesc; }
    public void setShoeDesc(String shoeDesc) { this.shoeDesc = shoeDesc; }

}
