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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import edu.vub.at.IAT;
import edu.vub.at.android.util.IATSettings.IATOptions;
import edu.vub.at.exceptions.InterpreterException;
import edu.vub.at.util.logging.Logging;

public class IATAndroid extends IAT {
		
	// This object ensures that the OS maintains its multicast functionality.
	@SuppressWarnings("unused")
	private MulticastLock mclock_;
	
	// State variables: The application name and the activity that started it.
	private String appName_ = null;
	private Activity parent_ = null;

	public static IATAndroid instance = null;
	public static final Object instance_lock = new Object();
	
	public static IATAndroid getInstance() {
		synchronized (instance_lock) {
			return instance;
		}
	}

	
	/* Create an IATAndroid object with the given activity as parent with the default iat option values */ 
	public static IATAndroid create(Activity parent) throws IOException, InterpreterException {
		return create(parent, IATSettings.getIATOptions(parent, parent.getExternalFilesDir(null)));
	}
	
	/* Create an IATAndroid object with the given activity as parent with custom iat option values */ 
	public static IATAndroid create(Activity parent, IATOptions iatOptions) throws IOException, InterpreterException {	
		File _ENV_AT_HOME_ = new File(iatOptions.AT_HOME_);
		File _ENV_AT_BASE_ = new File(iatOptions.AT_BASE_);
		
		System.setProperty("AT_HOME", iatOptions.AT_HOME_);
		System.setProperty("AT_INIT", iatOptions.AT_INIT_);
		System.setProperty("AT_STACK_SIZE", Constants._ENV_AT_STACKSIZE_);		
		
		String appName = parent.getApplicationInfo().packageName;
			
		setupLogging(iatOptions);
		MulticastLock mclock = setupMulticast(parent);
		
		
		LinkedList<String> args = new LinkedList<String>(Arrays.asList(
				"-a", iatOptions.ipAddress_,
				"-o", getNamespaceMappings(_ENV_AT_HOME_.toString(), _ENV_AT_BASE_.toString()),
				"-n", iatOptions.networkName_,
				"-nojline"));
		
		if (iatOptions.startFile_ != null)
			args.add(iatOptions.startFile_);
		
		synchronized (instance_lock) {
			instance = new IATAndroid(args.toArray(new String[args.size()]));
			instance.parent_  = parent;
			instance.appName_ = appName;
			instance.mclock_  = mclock;
		}
		Log.v("AmbientTalk", "IAT created");

		return instance;
	}	

	private IATAndroid(String[] args) throws InterpreterException {
		super(args);
	}

	/* Log stderr to a logfile. */
	private static void setupLogging(IATOptions iatOptions) {
		PrintStream logS = null;
		
		try {
			File path = new File(iatOptions.logFilePath_);
			path.getParentFile().mkdirs();
			logS = new PrintStream(new FileOutputStream(path));
			System.setErr(logS);
		} catch (Exception e) {
			Logging.Init_LOG.error("Cannot open log file", e);
		}
	}

	/* Acquire the multicast lock. This tells the hardware to listen for packets
	 * addressed to multicast IPs as well. */
	private static MulticastLock setupMulticast(Activity parent) {
		WifiManager wm = (WifiManager) parent.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		Logging.Network_LOG.debug("Got WifiManager: " + wm);

		if (wm != null) {
			MulticastLock mclock = wm.createMulticastLock("AmbientTalk");
			mclock.acquire();
			Logging.Network_LOG.debug("Acquired multicast lock: " + mclock);
			return mclock;
		} else {
			throw new RuntimeException("Could not acquire multicast lock");
		}
	}
	
	/* Emulate the behavior of the IAT start-script.
	 * Returns a string of the format "foo=/sdcard/android/<packagename>/files/foo:bar=/sdcard/..." */
	private static String getNamespaceMappings(String atHomeFullPath, String atBase) {
		StringBuilder sb = new StringBuilder(512);
		File atHome = new File(atHomeFullPath);
		String dirs[] = atHome.list();
		for( String dir : dirs) {
			File entry = new File(atHome, dir);
			if (entry.isDirectory() && !entry.isHidden())
				sb.append(dir).append('=').append(entry.toString()).append(':');
		}
		sb.append("tmp="+ new File(atBase, Constants._AT_TEMP_FILES_PATH).getAbsolutePath());
		return sb.toString();
	}
	
	public Activity getParentActivity() {
		return parent_;
	}
	
	public String getAppName() {
		return appName_;
	}
	
	private void showReportErrorDialog(final String message, final Throwable t) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
		AlertDialog sendErrorReportDialog = builder.create();
		sendErrorReportDialog.setTitle("AmbientTalk encountered an error");
		sendErrorReportDialog.setMessage("Do you want to report this error?\n" + t.getMessage());
		sendErrorReportDialog.setIcon(R.drawable.ic_dialog_alert);
		sendErrorReportDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ Constants._AT_EMAIL_ADDRESS_ });
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[AT crash report] " + message);
                String traceString = "";
                StackTraceElement[] trace = t.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                	traceString = traceString + (trace[i]).toString() + "\n";
                }
                emailIntent.putExtra(Intent.EXTRA_TEXT, traceString);
                getParentActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                dialog.dismiss();
                System.out.println(message);
        		System.exit(1);
			}
		});
		sendErrorReportDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				System.out.println(message);
				System.exit(1);
			}
		});
		sendErrorReportDialog.show();
	}
	
	// Override IAT abort() method to show an error dialog.
	protected void abort(final String message, final Exception e) {
		getParentActivity().runOnUiThread(new Runnable() { public void run() {
			showReportErrorDialog(message, e);
		}});
	}
}