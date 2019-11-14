package PowerSystemDrawingGUI;

import java.util.*;

public class MapUtility
{
    public static <K, V> LinkedHashMap<K, V> sortMapByKey(final Map<K, V> map, final SortingOrder sortingOrder) {
        final Comparator<Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(final Map.Entry<K, V> o1, final Map.Entry<K, V> o2) {
                return comparableCompare(o1.getKey(), o2.getKey(), sortingOrder);
            }
        };
        return sortMap(map, comparator);
    }
    
    private static <T> int comparableCompare(final T o1, final T o2, final SortingOrder sortingOrder) {
        final int compare = ((Comparable)o1).compareTo(o2);
        switch (sortingOrder) {
            case ASCENDING: {
                return compare;
            }
            case DESCENDING: {
                return -1 * compare;
            }
            default: {
                return 0;
            }
        }
    }
    
    public static <K, V> LinkedHashMap<K, V> sortMap(final Map<K, V> map, final Comparator<Map.Entry<K, V>> comparator) {
        final List<Map.Entry<K, V>> mapEntries = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(mapEntries, comparator);
        final LinkedHashMap<K, V> result = new LinkedHashMap<K, V>(map.size() + map.size() / 20);
        for (final Map.Entry<K, V> entry : mapEntries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    public enum SortingOrder
    {
        ASCENDING("ASCENDING", 0), 
        DESCENDING("DESCENDING", 1);
        
        private SortingOrder(final String s, final int n) {
        }
    }
}
