package com.example.chatdraw.Activities;

/*
AssistantActivity.java
Copyright (C) 2019 Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.AlertDialog;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.DialPlan;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.tools.Log;

public abstract class AssistantActivity  extends LinphoneGenericActivity {
    public static AccountCreator mAccountCreator;

    protected ImageView mBack;
    private AlertDialog mCountryPickerDialog;

    private CountryPicker mCountryPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mAbortCreation) {
            return;
        }

        if (mAccountCreator == null) {
            String url = LinphonePreferences.instance().getXmlrpcUrl();
            Core core = LinphoneManager.getCore();
            core.loadConfigFromXml(LinphonePreferences.instance().getDefaultDynamicConfigFile());
            mAccountCreator = core.createAccountCreator(url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mBack = null;
        mCountryPickerDialog = null;
        mCountryPicker = null;

        super.onDestroy();
    }

//    @Override
//    public void onCountryClicked(DialPlan dialPlan) {
//        if (mCountryPickerDialog != null) {
//            mCountryPickerDialog.dismiss();
//            mCountryPickerDialog = null;
//        }
//    }

    protected void createProxyConfigAndLeaveAssistant() {
        Core core = LinphoneManager.getCore();
        boolean useLinphoneDefaultValues =
                "sip.linphone.org".equals(mAccountCreator.getDomain());
        if (useLinphoneDefaultValues) {
            core.loadConfigFromXml(LinphonePreferences.instance().getLinphoneDynamicConfigFile());
        }

        ProxyConfig proxyConfig = mAccountCreator.createProxyConfig();

        if (useLinphoneDefaultValues) {
            // Restore default values
            core.loadConfigFromXml(LinphonePreferences.instance().getDefaultDynamicConfigFile());
        } else {
            // If this isn't a sip.linphone.org account, disable push notifications and enable
            // service notification, otherwise incoming calls won't work (most probably)
            LinphonePreferences.instance().setServiceNotificationVisibility(true);
            // LinphoneService.instance().getNotificationManager().startForeground();
        }

        if (proxyConfig == null) {
            Log.e("[Assistant] Account creator couldn't create proxy config");
            // TODO: display error message
        } else {
            if (proxyConfig.getDialPrefix() == null) {
                DialPlan dialPlan = getDialPlanForCurrentCountry();
                if (dialPlan != null) {
                    proxyConfig.setDialPrefix(dialPlan.getCountryCallingCode());
                }
            }

            goToLinphoneActivity();
        }
    }

    protected void goToLinphoneActivity() {
        Toast.makeText(this, "WORKING", Toast.LENGTH_SHORT).show();
//        boolean needsEchoCalibration =
//                LinphoneManager.getCore().isEchoCancellerCalibrationRequired();
//        boolean echoCalibrationDone =
//                LinphonePreferences.instance().isEchoCancellationCalibrationDone();
//        Log.i(
//                "[Assistant] Echo cancellation calibration required ? "
//                        + needsEchoCalibration
//                        + ", already done ? "
//                        + echoCalibrationDone);
//
//        Intent intent;
//        if (needsEchoCalibration && !echoCalibrationDone) {
//            intent = new Intent(this, EchoCancellerCalibrationAssistantActivity.class);
//        } else {
//            /*boolean openH264 = LinphonePreferences.instance().isOpenH264CodecDownloadEnabled();
//            boolean codecFound =
//                    LinphoneManager.getInstance().getOpenH264DownloadHelper().isCodecFound();
//            boolean abiSupported =
//                    Version.getCpuAbis().contains("armeabi-v7a")
//                            && !Version.getCpuAbis().contains("x86");
//            boolean androidVersionOk = Version.sdkStrictlyBelow(Build.VERSION_CODES.M);
//
//            if (openH264 && abiSupported && androidVersionOk && !codecFound) {
//                intent = new Intent(this, OpenH264DownloadAssistantActivity.class);
//            } else {*/
//            intent = new Intent(this, DialerActivity.class);
//            intent.addFlags(
//                    Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            // }
//        }
//        startActivity(intent);
    }

    protected void showPhoneNumberDialog() {
        new AlertDialog.Builder(this)
                .setTitle("What will my phone number be used for?")
                .setMessage("Your friends will find you more easily if you link...")
                .show();
    }

    protected void showAccountAlreadyExistsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Account already exist")
                .setMessage("This phone number is already used. Please type a different...")
                .show();
    }

    protected void showGenericErrorDialog(AccountCreator.Status status) {
        String message;

        switch (status) {
//            // TODO handle other possible status
//            case 1:
//                message = "Invalid phone number";
//                break;
//            case WrongActivationCode:
//                message = "Invalid activation code";
//                break;
//            case PhoneNumberOverused:
//                message = "Too much SMS have been sent to this number";
//                break;
            default:
                message = "Unknown error";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .show();
    }

//    protected void showCountryPickerDialog() {
//        if (mCountryPicker == null) {
//            mCountryPicker = new CountryPicker(this, this);
//        }
//        mCountryPickerDialog =
//                new AlertDialog.Builder(this).setView(mCountryPicker.getView()).show();
//    }

    protected DialPlan getDialPlanForCurrentCountry() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            String countryIso = tm.getNetworkCountryIso();
            return getDialPlanFromCountryCode(countryIso);
        } catch (Exception e) {
            Log.e("[Assistant] " + e);
        }
        return null;
    }

    protected String getDevicePhoneNumber() {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            return tm.getLine1Number();
        } catch (Exception e) {
            Log.e("[Assistant] " + e);
        }
        return null;
    }

    protected DialPlan getDialPlanFromPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) return null;

        for (DialPlan c : Factory.instance().getDialPlans()) {
            if (prefix.equalsIgnoreCase(c.getCountryCallingCode())) return c;
        }
        return null;
    }

    private DialPlan getDialPlanFromCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) return null;

        for (DialPlan c : Factory.instance().getDialPlans()) {
            if (countryCode.equalsIgnoreCase(c.getIsoCountryCode())) return c;
        }
        return null;
    }

    int arePhoneNumberAndPrefixOk(EditText prefixEditText, EditText phoneNumberEditText) {
        String prefix = prefixEditText.getText().toString();
        if (prefix.startsWith("+")) {
            prefix = prefix.substring(1);
        }

        String phoneNumber = phoneNumberEditText.getText().toString();
        return mAccountCreator.setPhoneNumber(phoneNumber, prefix);
    }

    protected String getErrorFromPhoneNumberStatus(int status) {
        AccountCreator.PhoneNumberStatus phoneNumberStatus =
                AccountCreator.PhoneNumberStatus.fromInt(status);
        switch (phoneNumberStatus) {
            case InvalidCountryCode:
                return "Country code invalid";
            case TooShort:
                return "Phone number too short";
            case TooLong:
                return "Phone number too long";
            case Invalid:
                return "Invalid phone number";
        }
        return null;
    }

    protected String getErrorFromUsernameStatus(AccountCreator.UsernameStatus status) {
        switch (status) {
            case Invalid:
                return "Username length invalid";
            case InvalidCharacters:
                return "Invalid character(s) found";
            case TooLong:
                return "Username too long";
            case TooShort:
                return "Username too short";
        }
        return null;
    }
}
