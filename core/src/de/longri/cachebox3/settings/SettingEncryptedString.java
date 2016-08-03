/* 
 * Copyright (C) 2011-2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.settings;


import de.longri.cachebox3.Utils;

/**
 *  im value intern ist die Einstellung verschl�sselt abgespeichert
 *  so wie sie dann in die DB geschrieben wird.
 */
public class SettingEncryptedString extends SettingLongString {

	public SettingEncryptedString(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType, SettingUsage usage) {
		super(name, category, modus, defaultValue, StoreType, usage);
	}

	// liefert die Einstellung im Klartext
	@Override
	public String getValue() {
		if (value == null)
			return value;
		else
			return Utils.decrypt(this.value);
	}

	// Liefert die verschl�sselte Einstellung zur�ck
	public String getEncryptedValue() {
		return this.value;
	}

	// liefert den Standardwert im Klartext
	@Override
	public String getDefaultValue() {
		return Utils.decrypt(this.defaultValue);
	}

	// liefert den verschl�sselten Standadwert
	public String getEncryptedDefaultValue() {
		return this.defaultValue;
	}

	// hiermit kann die Einstellung im Klartext �bergeben werden und wird sofort
	// verschl�sselt
	@Override
	public void setValue(String value) {
		String encrypted = "";
		if (value.length() > 0)
			encrypted = Utils.encrypt(value);
		if ((this.value != null) && (this.value.equals(encrypted)))
			return;
		this.value = encrypted;
		setDirty();
	}

	// hier kann die schon verschl�sselte Einstellung �bergeben werden.
	public void setEncryptedValue(String value) {
		if ((this.value != null) && (this.value.equals(value)))
			return;
		this.value = value;
		setDirty();
	}

}
