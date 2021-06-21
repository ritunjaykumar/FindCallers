package com.softgyan.findcallers.utils;

import com.softgyan.findcallers.models.DoctorModel;
import com.softgyan.findcallers.models.ElectricianModel;

public class Common {
    public static DoctorModel doctorModel = null;
    public static ElectricianModel electricianModel = null;
    public static int type = -1;

    public static void clearValue() {
        doctorModel = null;
        electricianModel = null;
        type = -1;
    }
}
