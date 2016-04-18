package datamodellogger.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DatabaseConnection {

        private final StringProperty InformationSystemName;
        private final StringProperty DBvendor;
        private final StringProperty DBurl;
        private final StringProperty DBuser;
        private final StringProperty DBname;
        private final StringProperty DBpassword;

	public DatabaseConnection(String InformationSystemName, String DBvendor, String DBurl, String DBuser, String DBname, String DBpassword) {
		this.InformationSystemName = new SimpleStringProperty(InformationSystemName);
		this.DBvendor = new SimpleStringProperty(DBvendor);
                this.DBurl = new SimpleStringProperty(DBurl);
		this.DBuser = new SimpleStringProperty(DBuser);
                this.DBname = new SimpleStringProperty(DBname);
		this.DBpassword = new SimpleStringProperty(DBpassword);
	}
	
	public String getInformationSystemName() {
		return InformationSystemName.get();
	}

	public void setInformationSystemName(String InformationSystemName) {
		this.InformationSystemName.set(InformationSystemName);
	}
	
	public StringProperty InformationSystemNameProperty() {
		return InformationSystemName;
	}

	public String getDBvendor() {
		return DBvendor.get();
	}

	public void setDBvendor(String DBvendor) {
		this.DBvendor.set(DBvendor);
	}
	
	public StringProperty DBvendorProperty() {
		return DBvendor;
	}
        
        public String getDBurl() {
		return DBurl.get();
	}

	public void setDBurl(String DBurl) {
		this.DBurl.set(DBurl);
	}
	
	public StringProperty DBurl() {
		return DBurl;
	}
        
        public String getDBuser() {
            return DBuser.get();
        }

        public void setDBuser(String DBuser) {
                this.DBuser.set(DBuser);
        }

        public StringProperty DBuser() {
                return DBuser;
        }
        
        public String getDBname() {
            return DBname.get();
        }

        public void setDBname(String DBname) {
                this.DBname.set(DBname);
        }

        public StringProperty DBname() {
                return DBname;
        }
        
        public String getDBpassword() {
                return DBpassword.get();
        }

        public void setDBpassword(String DBpassword) {
                this.DBpassword.set(DBpassword);
        }

        public StringProperty DBpassword() {
                return DBpassword;
        }
        
        
	
	@Override
	public String toString() {
            
		return getInformationSystemName() + "," + 
                       getDBvendor()              + "," + 
                       getDBurl()                 + "," + 
                       getDBname()                + "," + 
                       getDBuser()                + "," + 
                       getDBpassword();
	}
}