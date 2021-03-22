package pl.coderslab.pojo;

public class ExtendMacro extends BasicMacro {
    private double glycemicCharge;

    public ExtendMacro(){

    }

    public void setGlycemicCharge(double glycemicCharge) {
        this.glycemicCharge = glycemicCharge;
    }

    public double getGlycemicCharge() {
        return glycemicCharge;
    }
}
