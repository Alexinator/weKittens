/**
 * AmbientTalk/2 Project
 * (c) Software Languages Lab, 2006 - 2011
 * Authors: Software Languages Lab - Ambient Group
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package edu.vub.at.android.util;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;
import edu.vub.at.util.logging.Logging;

public class IATSettings {
	
	// Default location for AmbientTalk library
	//public static final File _ENV_AT_HOME_ = new File(Constants._ENV_AT_BASE_, Constants._AT_HOME_RELATIVE_PATH_);
	//public static final File _ENV_AT_INIT_ = new File(_ENV_AT_HOME_, "/at/init/init.at");

	/* look through the available network interfaces and pick the first "decent" IPv4 address.
	 * As the emulator uses 10.0.2.15 by default, only use it if nothing better is available. */
	public static String getMyIp() {
		Set<String> eligible = eligibleIpAddresses();
		String ip;

		if (eligible.size() == 0)
			ip = "127.0.0.1";
		else
			ip = eligible.iterator().next();

		// for emulators bind to localhost, so that we can use port forwarding
		if (ip.equals("10.0.2.15") || ip.equals("10.0.2.16"))
			ip = "127.0.0.1";

		return ip;
	}

	public static Set<String> eligibleIpAddresses() {
		Set<String> eligible = new HashSet<String>();
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();
				while (address.hasMoreElements()) {
					InetAddress addr = address.nextElement();
					if (!addr.isLoopbackAddress() && !(addr.getHostAddress().indexOf(":") > -1)) {
						eligible.add(addr.getHostAddress());
					}
				}
			}
		} catch (Exception e) {
		}
		return eligible;
	}
	
	public static IATOptions getDefaultIATOptions(File baseDir){
		return new IATOptions(baseDir);
	}
	
	public static IATOptions getIATOptions(Activity a, File basedir) {
		SharedPreferences preferences = a.getSharedPreferences(Constants.IAT_SETTINGS_FILE, Context.MODE_PRIVATE);

		IATOptions opt = IATSettings.getDefaultIATOptions(basedir);
		opt.merge(preferences);
		return opt;
	}
	
	 public static class IATOptions implements Parcelable {
		 public String ipAddress_;
		 public String networkName_;
		 public String AT_HOME_;
		 public String AT_INIT_;
		 public String AT_BASE_;
		 public String logFilePath_;
		 public String startFile_;
		 
		 // construct default iat options.
		 IATOptions(File baseDir){
			 ipAddress_ = getMyIp();
			 networkName_ = "AmbientTalk";
			 AT_BASE_ = baseDir.getAbsolutePath();
			 AT_HOME_ = new File(baseDir, Constants._AT_HOME_RELATIVE_PATH_).getAbsolutePath();
			 AT_INIT_ = new File(AT_HOME_, Constants._AT_INIT_FILE_).getAbsolutePath();
			 logFilePath_ = new File(AT_HOME_,  "/at.log").getAbsolutePath();
		 }
		 
		 // construct a custom iat options instance.
		 IATOptions(String ipAddress, String networkName, String atHome, String atInit, String atBase){
			 ipAddress_ = ipAddress;
			 networkName_ = networkName;
			 AT_HOME_ = atHome;
			 AT_INIT_ = atInit;
			 AT_BASE_ = atBase;
		 }

		 // read options from the shared preferences file
		 public void merge(SharedPreferences p) {
			ipAddress_ = p.getString("ipAddress", ipAddress_);
			networkName_ =  p.getString("networkName", networkName_);
			AT_HOME_ = p.getString("AT_HOME", AT_HOME_);
			AT_INIT_ = p.getString("AT_INIT", AT_INIT_);
			AT_BASE_ = p.getString("AT_BASE", AT_BASE_);
			logFilePath_ = p.getString("logFilePath", logFilePath_);
		 }	
		 
		// Write options out to the preferences file
		public void writeToPreferences(Editor ep) {
			ep.putString("ipAddress", ipAddress_);
			ep.putString("networkName", networkName_);
			ep.putString("AT_HOME", AT_HOME_);
			ep.putString("AT_INIT", AT_INIT_);
			ep.putString("AT_BASE", AT_BASE_);
			ep.putString("logFilePath", logFilePath_);
		}

		 // Parcelable support
		 public int describeContents() {
			 return 0;
		 }
		 
		 public void writeToParcel(Parcel dest, int flags) {
			 dest.writeString(ipAddress_);
			 dest.writeString(networkName_);
			 dest.writeString(AT_HOME_);
			 dest.writeString(AT_INIT_);
			 dest.writeString(AT_BASE_);
			 dest.writeString(logFilePath_);
		 }
		 
		 private IATOptions(Parcel in) {
			 ipAddress_   = in.readString();
			 networkName_ = in.readString();
			 AT_HOME_     = in.readString();
			 AT_INIT_     = in.readString();
			 AT_BASE_     = in.readString();
			 logFilePath_ = in.readString();
		 }
		 
		 public static final Creator<IATOptions> CREATOR
		 	= new Creator<IATOptions>() {
			 public IATOptions createFromParcel(Parcel in) {
				 return new IATOptions(in);
			 }
			 
			 public IATOptions[] newArray(int size) {
				 return new IATOptions[size];
			 }
		 };

	 }

}
