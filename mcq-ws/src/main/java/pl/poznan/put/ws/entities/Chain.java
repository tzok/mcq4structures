package pl.poznan.put.ws.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.Valid;

@Entity
public class Chain {

  @Id private String name;

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
    return "Chain{" + "name='" + name + '\'' + ", residues=" + residues + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Chain chain = (Chain) o;
    return Objects.equals(name, chain.name) && Objects.equals(residues, chain.residues);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, residues);
  }
}
