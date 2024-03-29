public class CountDistance {
    public static double distance(double xCurrent, double yCurrent, double xNext, double yNext){
        return java.lang.Math.sqrt( ((xCurrent - xNext) * (xCurrent - xNext) ) + ((yCurrent - yNext) * (yCurrent - yNext)) );
    }

}
