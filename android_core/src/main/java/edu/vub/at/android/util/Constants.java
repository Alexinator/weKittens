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

import android.app.Activity;
import android.os.Environment;

public interface Constants {
	
	// Default stack size for new event loops
	public static final String _ENV_AT_STACKSIZE_ = "524288";
	
	// Mount point of the 'primary media directory', Uusually '/sdcard'
	//public static final File   _ENV_AT_BASE_ = Environment.getExternalStorageDirectory();

	// Folder under which the assets can be found (see AssetInstaller).
	public static final String _ENV_AT_ASSETS_BASE_ = "atlib";
	
	// Relative paths to at-home 
	public static final String _AT_HOME_RELATIVE_PATH_ = "files/";
	public static final String _AT_TEMP_FILES_PATH = "cache/";

	public static final String _AT_INIT_FILE_ = "/at/init/init.at";
	
	// Result code when AT installation fails
	public static final int _RESULT_FAIL_ = Activity.RESULT_FIRST_USER;

	// Name of shared preferences file
	public static final String IAT_SETTINGS_FILE = "IatSettings";
	
	// AmbientTalk email adress for reporting errors
	public static final String _AT_EMAIL_ADDRESS_	= "ambienttalk@gmail.com";

}