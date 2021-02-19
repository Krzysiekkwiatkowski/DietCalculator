package pl.coderslab.pojo;

public class GraphResult {
    private String description;
    private int percentage;
    private String styles;
    private boolean borders;

    public GraphResult(String description, int percentage, String styles, boolean borders){
        this.description = description;
        this.percentage = percentage;
        this.styles = styles;
        this.borders = borders;
    }

    public String getDescription() {
        return description;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getStyles(){
        return styles;
    }

    public boolean isBorders(){
        return borders;
    }
}
