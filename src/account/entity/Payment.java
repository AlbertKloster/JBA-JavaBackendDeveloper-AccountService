package account.entity;

import account.utils.DateParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
    private String employee;
    private LocalDate period;
    private Long salary;

    public void setPeriod(String period) {
        String regex = "((0[1-9])|(1[0-2]))-\\d{4}";
        if (period.matches(regex))
            this.period = DateParser.parse(period);
        else
            this.period = LocalDate.ofYearDay(1, 1);
    }

    public String getPeriod() {
        return DateParser.parse(period);
    }

}
