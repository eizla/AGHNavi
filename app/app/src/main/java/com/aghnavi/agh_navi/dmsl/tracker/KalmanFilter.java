package com.aghnavi.agh_navi.dmsl.tracker;


import com.google.android.gms.maps.model.LatLng;
import Jama.Matrix;

public class KalmanFilter {

    // http://math.nist.gov/javanumerics/jama/doc/
    // the variance of the positioning error
    private static final double sigmaR = 2;
    // wifi library update every 1 second
    private static final double dt = 1;
    // uncertainty in the systemF dynamics
    private static final double sigmaQ = 0.1 * Math.sqrt(2 / Math.PI);

    private final Matrix GQGTrans;

    private final Matrix F;
    private final Matrix M;
    private final Matrix R;

    private Matrix p;
    private Matrix x;

    public KalmanFilter(double lat0, double lot0) {

        Matrix Q = new Matrix(new double[][] { { sigmaQ * sigmaQ, 0 }, { 0, sigmaQ * sigmaQ } });
        R = new Matrix(new double[][] { { sigmaR * sigmaR, 0 }, { 0, sigmaR * sigmaR } });
        F = new Matrix(new double[][] { { 1, 0, dt, 0 }, { 0, 1, 0, dt }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } });
        Matrix G = new Matrix(new double[][] { { 0, 0 }, { 0, 0 }, { dt, 0 }, { 0, dt } });
        M = new Matrix(new double[][] { { 1, 0, 0, 0 }, { 0, 1, 0, 0 } });
        p = new Matrix(new double[][] { { sigmaR * sigmaR, 0, 0, 0 }, { 0, sigmaR * sigmaR, 0, 0 }, { 0, 0, 15 * 15, 0 }, { 0, 0, 0, 15 * 15 } });

        GQGTrans = G.times(Q).times(G.transpose());
        reset(lat0, lot0);
    }

    void reset(double lat0, double lot0) {
        x = new Matrix(new double[][] { { lat0 }, { lot0 }, { 0 }, { 0 } });
    }

    public LatLng update(double lat, double lot) {
        // predict
        Matrix x_bar = F.times(x);
        Matrix p_bar = (F.times(p).times(F.transpose())).plus(GQGTrans);
        // Update
        Matrix mpmr = (M.times(p_bar).times(M.transpose()).plus(R)).inverse();
        Matrix k = p_bar.times(M.transpose()).times(mpmr);
        Matrix Y = new Matrix(new double[][] { { lat }, { lot } });
        x = x_bar.plus(k.times(Y.minus(M.times(x_bar))));
        p = (Matrix.identity(4, 4).minus(k.times(M))).times(p_bar);
        return new LatLng(x.get(0, 0), x.get(1, 0));
    }
}
