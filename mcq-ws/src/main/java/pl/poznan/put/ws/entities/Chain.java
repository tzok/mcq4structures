package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Chain {

  @Id
  private String name;

  @Valid
  @OneToMany(targetEntity = Residue.class, mappedBy = "residueNumber")
  private List<Residue> residues = new ArrayList<Residue>();

  public Chain() {}

  public Chain(String name, List<Residue> residues) {
    super();
    this.name = name;
    this.residues = residues;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Residue> getResidues() {
    return residues;
  }

  public void setResidues(List<Residue> residues) {
    this.residues = residues;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Chain.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("name");
    sb.append('=');
    sb.append(((this.name == null) ? "<null>" : this.name));
    sb.append(',');
    sb.append("residues");
    sb.append('=');
    sb.append(((this.residues == null) ? "<null>" : this.residues));
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
    result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
    result = ((result * 31) + ((this.residues == null) ? 0 : this.residues.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Chain) == false) {
      return false;
    }
    Chain rhs = ((Chain) other);
    return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
        && ((this.residues == rhs.residues)
            || ((this.residues != null) && this.residues.equals(rhs.residues))));
  }
}
