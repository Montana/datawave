package datawave.microservice.query.logic;

import datawave.webservice.query.Query;
import datawave.webservice.query.QueryImpl;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A query checkpoint will be very different depending on the query logic. It is expected that whatever the query state is can be encoded in a map of
 * properties.
 */
public class QueryCheckpoint implements Serializable {
    private static final long serialVersionUID = -9201879510622137934L;
    
    private final QueryKey queryKey;
    private final Map<String,Object> properties;
    
    public QueryCheckpoint(String queryPool, String queryId, String queryLogic, Query query) {
        this(queryPool, queryId, queryLogic, queryToProperties(query));
    }
    
    public QueryCheckpoint(String queryPool, String queryId, String queryLogic, Map<String,Object> properties) {
        this(new QueryKey(queryPool, queryId, queryLogic), properties);
    }
    
    public QueryCheckpoint(QueryKey queryKey, Query query) {
        this(queryKey, queryToProperties(query));
    }
    
    public QueryCheckpoint(QueryKey queryKey, Map<String,Object> properties) {
        this.queryKey = queryKey;
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }
    
    /**
     * Get the query key
     *
     * @return the query key
     */
    public QueryKey getQueryKey() {
        return queryKey;
    }
    
    /**
     * Get the properties representing the state of the query.
     * 
     * @return The properties
     */
    public Map<String,Object> getProperties() {
        return properties;
    }
    
    /**
     * Return the properties as a Query object.
     *
     * @return a Query
     */
    public Query getPropertiesAsQuery() throws ParseException {
        return propertiesToQuery(properties);
    }
    
    @Override
    public String toString() {
        return getQueryKey() + ": " + getProperties();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof QueryCheckpoint) {
            QueryCheckpoint other = (QueryCheckpoint) o;
            return new EqualsBuilder().append(getQueryKey(), other.getQueryKey()).append(getProperties(), other.getProperties()).isEquals();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getQueryKey()).append(getProperties()).toHashCode();
    }
    
    /**
     * Convert a query to properties that can be put in a checkpoint
     *
     * @param query
     *            The query
     * @return properties
     */
    public static Map<String,Object> queryToProperties(Query query) {
        return query.toMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> (e.getValue().size() == 1 ? e.getValue().get(0) : e.getValue())));
    }
    
    /**
     * Convert a set of properties to a query
     * 
     * @param props
     *            The properties
     * @return the query
     */
    public static Query propertiesToQuery(Map<String,Object> props) throws ParseException {
        Query query = new QueryImpl();
        MultiValueMap<String,String> queryMap = new LinkedMultiValueMap<>();
        props.entrySet().forEach(e -> {
            if (e.getValue() instanceof List)
                queryMap.put(e.getKey(), (List) e.getValue());
            else
                queryMap.set(e.getKey(), e.getValue().toString());
        });
        query.readMap(queryMap);
        return query;
    }
}