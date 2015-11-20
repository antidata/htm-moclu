package com.github.antidata.settings;

import org.numenta.nupic.Parameters;
import org.numenta.nupic.util.Tuple;

import java.util.HashMap;
import java.util.Map;

public class NetworkParameters {
    public static Parameters getParameters() {
        Parameters parameters = Parameters.getAllDefaultParameters();
        parameters.setParameterByKey(Parameters.KEY.INPUT_DIMENSIONS, new int[]{8});
        parameters.setParameterByKey(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{20});
        parameters.setParameterByKey(Parameters.KEY.CELLS_PER_COLUMN, Integer.valueOf(6));
        parameters.setParameterByKey(Parameters.KEY.POTENTIAL_RADIUS, Integer.valueOf(12));
        parameters.setParameterByKey(Parameters.KEY.POTENTIAL_PCT, Double.valueOf(0.5D));
        parameters.setParameterByKey(Parameters.KEY.GLOBAL_INHIBITIONS, Boolean.valueOf(false));
        parameters.setParameterByKey(Parameters.KEY.LOCAL_AREA_DENSITY, Double.valueOf(-1.0D));
        parameters.setParameterByKey(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, Double.valueOf(5.0D));
        parameters.setParameterByKey(Parameters.KEY.STIMULUS_THRESHOLD, Double.valueOf(1.0D));
        parameters.setParameterByKey(Parameters.KEY.SYN_PERM_INACTIVE_DEC, Double.valueOf(0.01D));
        parameters.setParameterByKey(Parameters.KEY.SYN_PERM_ACTIVE_INC, Double.valueOf(0.1D));
        parameters.setParameterByKey(Parameters.KEY.SYN_PERM_TRIM_THRESHOLD, Double.valueOf(0.05D));
        parameters.setParameterByKey(Parameters.KEY.SYN_PERM_CONNECTED, Double.valueOf(0.1D));
        parameters.setParameterByKey(Parameters.KEY.MIN_PCT_OVERLAP_DUTY_CYCLE, Double.valueOf(0.1D));
        parameters.setParameterByKey(Parameters.KEY.MIN_PCT_ACTIVE_DUTY_CYCLE, Double.valueOf(0.1D));
        parameters.setParameterByKey(Parameters.KEY.DUTY_CYCLE_PERIOD, Integer.valueOf(10));
        parameters.setParameterByKey(Parameters.KEY.MAX_BOOST, Double.valueOf(10.0D));
        parameters.setParameterByKey(Parameters.KEY.SEED, Integer.valueOf(42));
        parameters.setParameterByKey(Parameters.KEY.SP_VERBOSITY, Integer.valueOf(0));
        parameters.setParameterByKey(Parameters.KEY.INITIAL_PERMANENCE, Double.valueOf(0.2D));
        parameters.setParameterByKey(Parameters.KEY.CONNECTED_PERMANENCE, Double.valueOf(0.8D));
        parameters.setParameterByKey(Parameters.KEY.MIN_THRESHOLD, Integer.valueOf(5));
        parameters.setParameterByKey(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, Integer.valueOf(6));
        parameters.setParameterByKey(Parameters.KEY.PERMANENCE_INCREMENT, Double.valueOf(0.05D));
        parameters.setParameterByKey(Parameters.KEY.PERMANENCE_DECREMENT, Double.valueOf(0.05D));
        parameters.setParameterByKey(Parameters.KEY.ACTIVATION_THRESHOLD, Integer.valueOf(4));
        return parameters;
    }

    public static Map<String, Map<String, Object>> setupMap(Map<String, Map<String, Object>> map, int n, int w, double min, double max, double radius, double resolution, Boolean periodic, Boolean clip, Boolean forced, String fieldName, String fieldType, String encoderType) {
        if(map == null) {
            map = new HashMap();
        }

        Object inner = null;
        if((inner = (Map)((Map)map).get(fieldName)) == null) {
            ((Map)map).put(fieldName, inner = new HashMap());
        }

        ((Map)inner).put("n", Integer.valueOf(n));
        ((Map)inner).put("w", Integer.valueOf(w));
        ((Map)inner).put("minVal", Double.valueOf(min));
        ((Map)inner).put("maxVal", Double.valueOf(max));
        ((Map)inner).put("radius", Double.valueOf(radius));
        ((Map)inner).put("resolution", Double.valueOf(resolution));
        if(periodic != null) {
            ((Map)inner).put("periodic", periodic);
        }

        if(clip != null) {
            ((Map)inner).put("clipInput", clip);
        }

        if(forced != null) {
            ((Map)inner).put("forced", forced);
        }

        if(fieldName != null) {
            ((Map)inner).put("fieldName", fieldName);
        }

        if(fieldType != null) {
            ((Map)inner).put("fieldType", fieldType);
        }

        if(encoderType != null) {
            ((Map)inner).put("encoderType", encoderType);
            if(encoderType == "GeospatialCoordinateEncoder") {
                //((Map)inner).put("scale", 30);
                //((Map)inner).put("timestep", 60);
            }
        }

        return (Map)map;
    }

    public static Map<String, Map<String, Object>> getNetworkDemoFieldEncodingMap() {
        Map fieldEncodings = setupMap((Map)null, 0, 0, 0.0D, 0.0D, 0.0D, 0.0D, (Boolean)null, (Boolean)null, (Boolean)null, "timestamp", "datetime", "DateEncoder");
        fieldEncodings = setupMap(fieldEncodings, 50, 21, 0.0D, 360.0D, 0.0D, 0.1D, (Boolean)null, Boolean.TRUE, (Boolean)null, "consumption", "float", "ScalarEncoder");
        fieldEncodings = setupMap(fieldEncodings, 999, 25, 0.0D, 100.0D, 0.0D, 0.1D, (Boolean)null, Boolean.TRUE, (Boolean)null, "location", "geo", "GeospatialCoordinateEncoder");
        ((Map)fieldEncodings.get("timestamp")).put(Parameters.KEY.DATEFIELD_TOFD.getFieldName(), new Tuple(new Object[]{Integer.valueOf(21), Double.valueOf(9.5D)}));
        ((Map)fieldEncodings.get("timestamp")).put(Parameters.KEY.DATEFIELD_PATTERN.getFieldName(), "MM/dd/YY HH:mm");
        ((Map)fieldEncodings.get("location")).put("timestep", "60");
        ((Map)fieldEncodings.get("location")).put("scale", "30");
        return fieldEncodings;
    }

    public static Parameters getNetworkEncoderParams() {
        Map fieldEncodings = getNetworkDemoFieldEncodingMap();
        Parameters p = Parameters.getEncoderDefaultParameters();
        p.setParameterByKey(Parameters.KEY.GLOBAL_INHIBITIONS, Boolean.valueOf(true));
        p.setParameterByKey(Parameters.KEY.COLUMN_DIMENSIONS, new int[]{2048});
        p.setParameterByKey(Parameters.KEY.CELLS_PER_COLUMN, Integer.valueOf(32));
        p.setParameterByKey(Parameters.KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, Double.valueOf(40.0D));
        p.setParameterByKey(Parameters.KEY.POTENTIAL_PCT, Double.valueOf(0.8D));
        p.setParameterByKey(Parameters.KEY.SYN_PERM_CONNECTED, Double.valueOf(0.1D));
        p.setParameterByKey(Parameters.KEY.SYN_PERM_ACTIVE_INC, Double.valueOf(1.0E-4D));
        p.setParameterByKey(Parameters.KEY.SYN_PERM_INACTIVE_DEC, Double.valueOf(5.0E-4D));
        p.setParameterByKey(Parameters.KEY.MAX_BOOST, Double.valueOf(1.0D));
        p.setParameterByKey(Parameters.KEY.MAX_NEW_SYNAPSE_COUNT, Integer.valueOf(20));
        p.setParameterByKey(Parameters.KEY.INITIAL_PERMANENCE, Double.valueOf(0.21D));
        p.setParameterByKey(Parameters.KEY.PERMANENCE_INCREMENT, Double.valueOf(0.1D));
        p.setParameterByKey(Parameters.KEY.PERMANENCE_DECREMENT, Double.valueOf(0.1D));
        p.setParameterByKey(Parameters.KEY.MIN_THRESHOLD, Integer.valueOf(9));
        p.setParameterByKey(Parameters.KEY.ACTIVATION_THRESHOLD, Integer.valueOf(12));
        p.setParameterByKey(Parameters.KEY.CLIP_INPUT, Boolean.valueOf(true));
        p.setParameterByKey(Parameters.KEY.FIELD_ENCODING_MAP, fieldEncodings);
        return p;
    }

    public static Parameters getModelParameters() {
        return getParameters().union(getNetworkEncoderParams());
    }
}
