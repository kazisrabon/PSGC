package PowerSystemDrawingGUI;

import org.jgrapht.graph.DefaultEdge;

public class RelationshipEdge extends
        DefaultEdge
{
    private String label;
    public RelationshipEdge(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }
}