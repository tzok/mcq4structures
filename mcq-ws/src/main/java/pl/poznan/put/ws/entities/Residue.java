package pl.poznan.put.ws.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.Id;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "isMissing",
  "standardResidueName",
  "modifiedResidueName",
  "oneLetterName",
  "chain",
  "residueNumber",
  "insertionCode",
  "alpha",
  "beta",
  "gamma",
  "delta",
  "epsilon",
  "zeta",
  "nu0",
  "nu1",
  "nu2",
  "nu3",
  "nu4",
  "eta",
  "theta",
  "etaPrim",
  "tehtaPrim",
  "chi",
  "pseudophasePucker"
})
@Entity
@Generated("jsonschema2pojo")
public class Residue {

  @JsonProperty("isMissing")
  private boolean isMissing;

  @JsonProperty("standardResidueName")
  private String standardResidueName;

  @JsonProperty("modifiedResidueName")
  private String modifiedResidueName;

  @JsonProperty("oneLetterName")
  private String oneLetterName;

  @JsonProperty("chain")
  private String chain;

  @JsonProperty("residueNumber")
  @Id
  private int residueNumber;

  @JsonProperty("insertionCode")
  private String insertionCode;

  @JsonProperty("alpha")
  private double alpha;

  @JsonProperty("beta")
  private double beta;

  @JsonProperty("gamma")
  private double gamma;

  @JsonProperty("delta")
  private double delta;

  @JsonProperty("epsilon")
  private double epsilon;

  @JsonProperty("zeta")
  private double zeta;

  @JsonProperty("nu0")
  private double nu0;

  @JsonProperty("nu1")
  private double nu1;

  @JsonProperty("nu2")
  private double nu2;

  @JsonProperty("nu3")
  private double nu3;

  @JsonProperty("nu4")
  private double nu4;

  @JsonProperty("eta")
  private double eta;

  @JsonProperty("theta")
  private double theta;

  @JsonProperty("etaPrim")
  private double etaPrim;

  @JsonProperty("tehtaPrim")
  private double tehtaPrim;

  @JsonProperty("chi")
  private double chi;

  @JsonProperty("pseudophasePucker")
  private double pseudophasePucker;

  /** No args constructor for use in serialization */
  public Residue() {}

  /**
   * @param zeta
   * @param chain
   * @param chi
   * @param delta
   * @param nu0
   * @param nu2
   * @param nu1
   * @param insertionCode
   * @param nu4
   * @param nu3
   * @param theta
   * @param standardResidueName
   * @param epsilon
   * @param isMissing
   * @param eta
   * @param residueNumber
   * @param alpha
   * @param modifiedResidueName
   * @param tehtaPrim
   * @param oneLetterName
   * @param pseudophasePucker
   * @param etaPrim
   * @param beta
   * @param gamma
   */
  public Residue(
      boolean isMissing,
      String standardResidueName,
      String modifiedResidueName,
      String oneLetterName,
      String chain,
      int residueNumber,
      String insertionCode,
      double alpha,
      double beta,
      double gamma,
      double delta,
      double epsilon,
      double zeta,
      double nu0,
      double nu1,
      double nu2,
      double nu3,
      double nu4,
      double eta,
      double theta,
      double etaPrim,
      double tehtaPrim,
      double chi,
      double pseudophasePucker) {
    super();
    this.isMissing = isMissing;
    this.standardResidueName = standardResidueName;
    this.modifiedResidueName = modifiedResidueName;
    this.oneLetterName = oneLetterName;
    this.chain = chain;
    this.residueNumber = residueNumber;
    this.insertionCode = insertionCode;
    this.alpha = alpha;
    this.beta = beta;
    this.gamma = gamma;
    this.delta = delta;
    this.epsilon = epsilon;
    this.zeta = zeta;
    this.nu0 = nu0;
    this.nu1 = nu1;
    this.nu2 = nu2;
    this.nu3 = nu3;
    this.nu4 = nu4;
    this.eta = eta;
    this.theta = theta;
    this.etaPrim = etaPrim;
    this.tehtaPrim = tehtaPrim;
    this.chi = chi;
    this.pseudophasePucker = pseudophasePucker;
  }

  @JsonProperty("isMissing")
  public boolean isIsMissing() {
    return isMissing;
  }

  @JsonProperty("isMissing")
  public void setIsMissing(boolean isMissing) {
    this.isMissing = isMissing;
  }

  @JsonProperty("standardResidueName")
  public String getStandardResidueName() {
    return standardResidueName;
  }

  @JsonProperty("standardResidueName")
  public void setStandardResidueName(String standardResidueName) {
    this.standardResidueName = standardResidueName;
  }

  @JsonProperty("modifiedResidueName")
  public String getModifiedResidueName() {
    return modifiedResidueName;
  }

  @JsonProperty("modifiedResidueName")
  public void setModifiedResidueName(String modifiedResidueName) {
    this.modifiedResidueName = modifiedResidueName;
  }

  @JsonProperty("oneLetterName")
  public String getOneLetterName() {
    return oneLetterName;
  }

  @JsonProperty("oneLetterName")
  public void setOneLetterName(String oneLetterName) {
    this.oneLetterName = oneLetterName;
  }

  @JsonProperty("chain")
  public String getChain() {
    return chain;
  }

  @JsonProperty("chain")
  public void setChain(String chain) {
    this.chain = chain;
  }

  @JsonProperty("residueNumber")
  public int getResidueNumber() {
    return residueNumber;
  }

  @JsonProperty("residueNumber")
  public void setResidueNumber(int residueNumber) {
    this.residueNumber = residueNumber;
  }

  @JsonProperty("insertionCode")
  public String getInsertionCode() {
    return insertionCode;
  }

  @JsonProperty("insertionCode")
  public void setInsertionCode(String insertionCode) {
    this.insertionCode = insertionCode;
  }

  @JsonProperty("alpha")
  public double getAlpha() {
    return alpha;
  }

  @JsonProperty("alpha")
  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }

  @JsonProperty("beta")
  public double getBeta() {
    return beta;
  }

  @JsonProperty("beta")
  public void setBeta(double beta) {
    this.beta = beta;
  }

  @JsonProperty("gamma")
  public double getGamma() {
    return gamma;
  }

  @JsonProperty("gamma")
  public void setGamma(double gamma) {
    this.gamma = gamma;
  }

  @JsonProperty("delta")
  public double getDelta() {
    return delta;
  }

  @JsonProperty("delta")
  public void setDelta(double delta) {
    this.delta = delta;
  }

  @JsonProperty("epsilon")
  public double getEpsilon() {
    return epsilon;
  }

  @JsonProperty("epsilon")
  public void setEpsilon(double epsilon) {
    this.epsilon = epsilon;
  }

  @JsonProperty("zeta")
  public double getZeta() {
    return zeta;
  }

  @JsonProperty("zeta")
  public void setZeta(double zeta) {
    this.zeta = zeta;
  }

  @JsonProperty("nu0")
  public double getNu0() {
    return nu0;
  }

  @JsonProperty("nu0")
  public void setNu0(double nu0) {
    this.nu0 = nu0;
  }

  @JsonProperty("nu1")
  public double getNu1() {
    return nu1;
  }

  @JsonProperty("nu1")
  public void setNu1(double nu1) {
    this.nu1 = nu1;
  }

  @JsonProperty("nu2")
  public double getNu2() {
    return nu2;
  }

  @JsonProperty("nu2")
  public void setNu2(double nu2) {
    this.nu2 = nu2;
  }

  @JsonProperty("nu3")
  public double getNu3() {
    return nu3;
  }

  @JsonProperty("nu3")
  public void setNu3(double nu3) {
    this.nu3 = nu3;
  }

  @JsonProperty("nu4")
  public double getNu4() {
    return nu4;
  }

  @JsonProperty("nu4")
  public void setNu4(double nu4) {
    this.nu4 = nu4;
  }

  @JsonProperty("eta")
  public double getEta() {
    return eta;
  }

  @JsonProperty("eta")
  public void setEta(double eta) {
    this.eta = eta;
  }

  @JsonProperty("theta")
  public double getTheta() {
    return theta;
  }

  @JsonProperty("theta")
  public void setTheta(double theta) {
    this.theta = theta;
  }

  @JsonProperty("etaPrim")
  public double getEtaPrim() {
    return etaPrim;
  }

  @JsonProperty("etaPrim")
  public void setEtaPrim(double etaPrim) {
    this.etaPrim = etaPrim;
  }

  @JsonProperty("tehtaPrim")
  public double getTehtaPrim() {
    return tehtaPrim;
  }

  @JsonProperty("tehtaPrim")
  public void setTehtaPrim(double tehtaPrim) {
    this.tehtaPrim = tehtaPrim;
  }

  @JsonProperty("chi")
  public double getChi() {
    return chi;
  }

  @JsonProperty("chi")
  public void setChi(double chi) {
    this.chi = chi;
  }

  @JsonProperty("pseudophasePucker")
  public double getPseudophasePucker() {
    return pseudophasePucker;
  }

  @JsonProperty("pseudophasePucker")
  public void setPseudophasePucker(double pseudophasePucker) {
    this.pseudophasePucker = pseudophasePucker;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(Residue.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("isMissing");
    sb.append('=');
    sb.append(this.isMissing);
    sb.append(',');
    sb.append("standardResidueName");
    sb.append('=');
    sb.append(((this.standardResidueName == null) ? "<null>" : this.standardResidueName));
    sb.append(',');
    sb.append("modifiedResidueName");
    sb.append('=');
    sb.append(((this.modifiedResidueName == null) ? "<null>" : this.modifiedResidueName));
    sb.append(',');
    sb.append("oneLetterName");
    sb.append('=');
    sb.append(((this.oneLetterName == null) ? "<null>" : this.oneLetterName));
    sb.append(',');
    sb.append("chain");
    sb.append('=');
    sb.append(((this.chain == null) ? "<null>" : this.chain));
    sb.append(',');
    sb.append("residueNumber");
    sb.append('=');
    sb.append(this.residueNumber);
    sb.append(',');
    sb.append("insertionCode");
    sb.append('=');
    sb.append(((this.insertionCode == null) ? "<null>" : this.insertionCode));
    sb.append(',');
    sb.append("alpha");
    sb.append('=');
    sb.append(this.alpha);
    sb.append(',');
    sb.append("beta");
    sb.append('=');
    sb.append(this.beta);
    sb.append(',');
    sb.append("gamma");
    sb.append('=');
    sb.append(this.gamma);
    sb.append(',');
    sb.append("delta");
    sb.append('=');
    sb.append(this.delta);
    sb.append(',');
    sb.append("epsilon");
    sb.append('=');
    sb.append(this.epsilon);
    sb.append(',');
    sb.append("zeta");
    sb.append('=');
    sb.append(this.zeta);
    sb.append(',');
    sb.append("nu0");
    sb.append('=');
    sb.append(this.nu0);
    sb.append(',');
    sb.append("nu1");
    sb.append('=');
    sb.append(this.nu1);
    sb.append(',');
    sb.append("nu2");
    sb.append('=');
    sb.append(this.nu2);
    sb.append(',');
    sb.append("nu3");
    sb.append('=');
    sb.append(this.nu3);
    sb.append(',');
    sb.append("nu4");
    sb.append('=');
    sb.append(this.nu4);
    sb.append(',');
    sb.append("eta");
    sb.append('=');
    sb.append(this.eta);
    sb.append(',');
    sb.append("theta");
    sb.append('=');
    sb.append(this.theta);
    sb.append(',');
    sb.append("etaPrim");
    sb.append('=');
    sb.append(this.etaPrim);
    sb.append(',');
    sb.append("tehtaPrim");
    sb.append('=');
    sb.append(this.tehtaPrim);
    sb.append(',');
    sb.append("chi");
    sb.append('=');
    sb.append(this.chi);
    sb.append(',');
    sb.append("pseudophasePucker");
    sb.append('=');
    sb.append(this.pseudophasePucker);
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
                (Double.doubleToLongBits(this.delta)
                    ^ (Double.doubleToLongBits(this.delta) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.nu0) ^ (Double.doubleToLongBits(this.nu0) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.nu2) ^ (Double.doubleToLongBits(this.nu2) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.nu1) ^ (Double.doubleToLongBits(this.nu1) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.nu4) ^ (Double.doubleToLongBits(this.nu4) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.nu3) ^ (Double.doubleToLongBits(this.nu3) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.theta)
                    ^ (Double.doubleToLongBits(this.theta) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.epsilon)
                    ^ (Double.doubleToLongBits(this.epsilon) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.eta) ^ (Double.doubleToLongBits(this.eta) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.alpha)
                    ^ (Double.doubleToLongBits(this.alpha) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.tehtaPrim)
                    ^ (Double.doubleToLongBits(this.tehtaPrim) >>> 32))));
    result = ((result * 31) + ((this.oneLetterName == null) ? 0 : this.oneLetterName.hashCode()));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.pseudophasePucker)
                    ^ (Double.doubleToLongBits(this.pseudophasePucker) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.etaPrim)
                    ^ (Double.doubleToLongBits(this.etaPrim) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.beta)
                    ^ (Double.doubleToLongBits(this.beta) >>> 32))));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.zeta)
                    ^ (Double.doubleToLongBits(this.zeta) >>> 32))));
    result = ((result * 31) + ((this.chain == null) ? 0 : this.chain.hashCode()));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.chi) ^ (Double.doubleToLongBits(this.chi) >>> 32))));
    result = ((result * 31) + ((this.insertionCode == null) ? 0 : this.insertionCode.hashCode()));
    result =
        ((result * 31)
            + ((this.standardResidueName == null) ? 0 : this.standardResidueName.hashCode()));
    result = ((result * 31) + (this.isMissing ? 1 : 0));
    result = ((result * 31) + this.residueNumber);
    result =
        ((result * 31)
            + ((this.modifiedResidueName == null) ? 0 : this.modifiedResidueName.hashCode()));
    result =
        ((result * 31)
            + ((int)
                (Double.doubleToLongBits(this.gamma)
                    ^ (Double.doubleToLongBits(this.gamma) >>> 32))));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof Residue) == false) {
      return false;
    }
    Residue rhs = ((Residue) other);
    return ((((((((((((((((((((((((Double.doubleToLongBits(this.delta)
                                                                                                    == Double
                                                                                                        .doubleToLongBits(
                                                                                                            rhs.delta))
                                                                                                && (Double
                                                                                                        .doubleToLongBits(
                                                                                                            this
                                                                                                                .nu0)
                                                                                                    == Double
                                                                                                        .doubleToLongBits(
                                                                                                            rhs.nu0)))
                                                                                            && (Double
                                                                                                    .doubleToLongBits(
                                                                                                        this
                                                                                                            .nu2)
                                                                                                == Double
                                                                                                    .doubleToLongBits(
                                                                                                        rhs.nu2)))
                                                                                        && (Double
                                                                                                .doubleToLongBits(
                                                                                                    this
                                                                                                        .nu1)
                                                                                            == Double
                                                                                                .doubleToLongBits(
                                                                                                    rhs.nu1)))
                                                                                    && (Double
                                                                                            .doubleToLongBits(
                                                                                                this
                                                                                                    .nu4)
                                                                                        == Double
                                                                                            .doubleToLongBits(
                                                                                                rhs.nu4)))
                                                                                && (Double
                                                                                        .doubleToLongBits(
                                                                                            this
                                                                                                .nu3)
                                                                                    == Double
                                                                                        .doubleToLongBits(
                                                                                            rhs.nu3)))
                                                                            && (Double
                                                                                    .doubleToLongBits(
                                                                                        this.theta)
                                                                                == Double
                                                                                    .doubleToLongBits(
                                                                                        rhs.theta)))
                                                                        && (Double.doubleToLongBits(
                                                                                this.epsilon)
                                                                            == Double
                                                                                .doubleToLongBits(
                                                                                    rhs.epsilon)))
                                                                    && (Double.doubleToLongBits(
                                                                            this.eta)
                                                                        == Double.doubleToLongBits(
                                                                            rhs.eta)))
                                                                && (Double.doubleToLongBits(
                                                                        this.alpha)
                                                                    == Double.doubleToLongBits(
                                                                        rhs.alpha)))
                                                            && (Double.doubleToLongBits(
                                                                    this.tehtaPrim)
                                                                == Double.doubleToLongBits(
                                                                    rhs.tehtaPrim)))
                                                        && ((this.oneLetterName
                                                                == rhs.oneLetterName)
                                                            || ((this.oneLetterName != null)
                                                                && this.oneLetterName.equals(
                                                                    rhs.oneLetterName))))
                                                    && (Double.doubleToLongBits(
                                                            this.pseudophasePucker)
                                                        == Double.doubleToLongBits(
                                                            rhs.pseudophasePucker)))
                                                && (Double.doubleToLongBits(this.etaPrim)
                                                    == Double.doubleToLongBits(rhs.etaPrim)))
                                            && (Double.doubleToLongBits(this.beta)
                                                == Double.doubleToLongBits(rhs.beta)))
                                        && (Double.doubleToLongBits(this.zeta)
                                            == Double.doubleToLongBits(rhs.zeta)))
                                    && ((this.chain == rhs.chain)
                                        || ((this.chain != null) && this.chain.equals(rhs.chain))))
                                && (Double.doubleToLongBits(this.chi)
                                    == Double.doubleToLongBits(rhs.chi)))
                            && ((this.insertionCode == rhs.insertionCode)
                                || ((this.insertionCode != null)
                                    && this.insertionCode.equals(rhs.insertionCode))))
                        && ((this.standardResidueName == rhs.standardResidueName)
                            || ((this.standardResidueName != null)
                                && this.standardResidueName.equals(rhs.standardResidueName))))
                    && (this.isMissing == rhs.isMissing))
                && (this.residueNumber == rhs.residueNumber))
            && ((this.modifiedResidueName == rhs.modifiedResidueName)
                || ((this.modifiedResidueName != null)
                    && this.modifiedResidueName.equals(rhs.modifiedResidueName))))
        && (Double.doubleToLongBits(this.gamma) == Double.doubleToLongBits(rhs.gamma)));
  }
}
