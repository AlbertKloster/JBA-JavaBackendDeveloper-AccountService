package account.utils;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.http.converter.json.MappingJacksonValue;

public class JacksonValueMapper {
    private final Object object;
    private final String filterId;

    public JacksonValueMapper(String filterId, Object object) {
        this.filterId = filterId;
        this.object = object;
    }

    public MappingJacksonValue map(String... field) {
        SimpleBeanPropertyFilter filter =
                SimpleBeanPropertyFilter.filterOutAllExcept(field);
        FilterProvider filters = new SimpleFilterProvider().addFilter(filterId, filter);
        MappingJacksonValue response = new MappingJacksonValue(object);
        response.setFilters(filters);
        return response;
    }

}
