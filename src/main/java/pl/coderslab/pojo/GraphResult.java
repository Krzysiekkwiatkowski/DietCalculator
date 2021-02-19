package pl.coderslab.pojo;

public class GraphResult {
    private String description;
    private String part;
    private String styles;
    private boolean borders;

    public GraphResult(String description, String part, String styles, boolean borders){
        this.description = description;
        this.part = part;
        this.styles = styles;
        this.borders = borders;
    }

    public String getDescription() {
        return description;
    }

    public String getPercentage() {
        return part;
    }

    public String getStyles(){
        return styles;
    }

    public boolean isBorders(){
        return borders;
    }
}
