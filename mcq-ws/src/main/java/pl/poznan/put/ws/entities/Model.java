package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"number", "chains"})
@Entity
@Generated("jsonschema2pojo")
public class Model {

  @JsonProperty("number")
  @Id
  private double number;

  @JsonProperty("chains")
  @Valid
  @OneToMany(targetEntity = Chain.class, mappedBy = "name")
  private List<Chain> chains = new ArrayList<Chain>();

  /** No args constructor for use in serialization */
  public Model() {}

  /**
   * @param number
   * @param chains
   */
  public Model(double number, List<Chain> chains) {
    super();
    this.number = number;
    this.chains = chains;
  }

  @JsonProperty("number")
  public double getNumber() {
    return number;
  }

  @JsonProperty("number")
  public void setNumber(double number) {
    this.number = number;
  }

  @JsonProperty("chains")
  public List<Chain> getChains() {
    return chains;
  }

  @JsonProperty("chains")
  public void setChains(List<Chain> chains) {
    this.chains = chains;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Model.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("number");
    sb.append('=');
    sb.append(this.number);
    sb.append(',');
    sb.append("chains");
    sb.append('=');
    sb.append(((this.chains == null) ? "<null>" : this.chains));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.number)
                    ^ (Double.doubleToLongBits(this.number) >>> 32))));
    result = ((result * 31) + ((this.chains == null) ? 0 : this.chains.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Model) == false) {
      return false;
    }
    Model rhs = ((Model) other);
    return ((Double.doubleToLongBits(this.number) == Double.doubleToLongBits(rhs.number))
        && ((this.chains == rhs.chains)
            || ((this.chains != null) && this.chains.equals(rhs.chains))));
  }
}
