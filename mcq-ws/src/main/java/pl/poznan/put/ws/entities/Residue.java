package pl.poznan.put.ws.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Residue {

  private boolean isMissing;

  private String standardResidueName;

  private String modifiedResidueName;

  private String oneLetterName;

  private String chain;

  @Id private int residueNumber;

  private String insertionCode;

  private double alpha;

  private double beta;

  private double gamma;

  private double delta;

  private double epsilon;

  private double zeta;

  private double nu0;

  private double nu1;

  private double nu2;

  private double nu3;

  private double nu4;

  private double eta;

  private double theta;

  private double etaPrim;

  private double tehtaPrim;

  private double chi;

  private double pseudophasePucker;

  public Residue() {}

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

  public boolean isIsMissing() {
    return isMissing;
  }

  public void setIsMissing(boolean isMissing) {
    this.isMissing = isMissing;
  }

  public String getStandardResidueName() {
    return standardResidueName;
  }

  public void setStandardResidueName(String standardResidueName) {
    this.standardResidueName = standardResidueName;
  }

  public String getModifiedResidueName() {
    return modifiedResidueName;
  }

  public void setModifiedResidueName(String modifiedResidueName) {
    this.modifiedResidueName = modifiedResidueName;
  }

  public String getOneLetterName() {
    return oneLetterName;
  }

  public void setOneLetterName(String oneLetterName) {
    this.oneLetterName = oneLetterName;
  }

  public String getChain() {
    return chain;
  }

  public void setChain(String chain) {
    this.chain = chain;
  }

  public int getResidueNumber() {
    return residueNumber;
  }

  public void setResidueNumber(int residueNumber) {
    this.residueNumber = residueNumber;
  }

  public String getInsertionCode() {
    return insertionCode;
  }

  public void setInsertionCode(String insertionCode) {
    this.insertionCode = insertionCode;
  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }

  public double getBeta() {
    return beta;
  }

  public void setBeta(double beta) {
    this.beta = beta;
  }

  public double getGamma() {
    return gamma;
  }

  public void setGamma(double gamma) {
    this.gamma = gamma;
  }

  public double getDelta() {
    return delta;
  }

  public void setDelta(double delta) {
    this.delta = delta;
  }

  public double getEpsilon() {
    return epsilon;
  }

  public void setEpsilon(double epsilon) {
    this.epsilon = epsilon;
  }

  public double getZeta() {
    return zeta;
  }

  public void setZeta(double zeta) {
    this.zeta = zeta;
  }

  public double getNu0() {
    return nu0;
  }

  public void setNu0(double nu0) {
    this.nu0 = nu0;
  }

  public double getNu1() {
    return nu1;
  }

  public void setNu1(double nu1) {
    this.nu1 = nu1;
  }

  public double getNu2() {
    return nu2;
  }

  public void setNu2(double nu2) {
    this.nu2 = nu2;
  }

  public double getNu3() {
    return nu3;
  }

  public void setNu3(double nu3) {
    this.nu3 = nu3;
  }

  public double getNu4() {
    return nu4;
  }

  public void setNu4(double nu4) {
    this.nu4 = nu4;
  }

  public double getEta() {
    return eta;
  }

  public void setEta(double eta) {
    this.eta = eta;
  }

  public double getTheta() {
    return theta;
  }

  public void setTheta(double theta) {
    this.theta = theta;
  }

  public double getEtaPrim() {
    return etaPrim;
  }

  public void setEtaPrim(double etaPrim) {
    this.etaPrim = etaPrim;
  }

  public double getTehtaPrim() {
    return tehtaPrim;
  }

  public void setTehtaPrim(double tehtaPrim) {
    this.tehtaPrim = tehtaPrim;
  }

  public double getChi() {
    return chi;
  }

  public void setChi(double chi) {
    this.chi = chi;
  }

  public double getPseudophasePucker() {
    return pseudophasePucker;
  }

  public void setPseudophasePucker(double pseudophasePucker) {
    this.pseudophasePucker = pseudophasePucker;
  }

  @Override
  public String toString() {
    return "Residue{"
        + "isMissing="
        + isMissing
        + ", standardResidueName='"
        + standardResidueName
        + '\''
        + ", modifiedResidueName='"
        + modifiedResidueName
        + '\''
        + ", oneLetterName='"
        + oneLetterName
        + '\''
        + ", chain='"
        + chain
        + '\''
        + ", residueNumber="
        + residueNumber
        + ", insertionCode='"
        + insertionCode
        + '\''
        + ", alpha="
        + alpha
        + ", beta="
        + beta
        + ", gamma="
        + gamma
        + ", delta="
        + delta
        + ", epsilon="
        + epsilon
        + ", zeta="
        + zeta
        + ", nu0="
        + nu0
        + ", nu1="
        + nu1
        + ", nu2="
        + nu2
        + ", nu3="
        + nu3
        + ", nu4="
        + nu4
        + ", eta="
        + eta
        + ", theta="
        + theta
        + ", etaPrim="
        + etaPrim
        + ", tehtaPrim="
        + tehtaPrim
        + ", chi="
        + chi
        + ", pseudophasePucker="
        + pseudophasePucker
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Residue residue = (Residue) o;
    return isMissing == residue.isMissing
        && residueNumber == residue.residueNumber
        && Double.compare(residue.alpha, alpha) == 0
        && Double.compare(residue.beta, beta) == 0
        && Double.compare(residue.gamma, gamma) == 0
        && Double.compare(residue.delta, delta) == 0
        && Double.compare(residue.epsilon, epsilon) == 0
        && Double.compare(residue.zeta, zeta) == 0
        && Double.compare(residue.nu0, nu0) == 0
        && Double.compare(residue.nu1, nu1) == 0
        && Double.compare(residue.nu2, nu2) == 0
        && Double.compare(residue.nu3, nu3) == 0
        && Double.compare(residue.nu4, nu4) == 0
        && Double.compare(residue.eta, eta) == 0
        && Double.compare(residue.theta, theta) == 0
        && Double.compare(residue.etaPrim, etaPrim) == 0
        && Double.compare(residue.tehtaPrim, tehtaPrim) == 0
        && Double.compare(residue.chi, chi) == 0
        && Double.compare(residue.pseudophasePucker, pseudophasePucker) == 0
        && Objects.equals(standardResidueName, residue.standardResidueName)
        && Objects.equals(modifiedResidueName, residue.modifiedResidueName)
        && Objects.equals(oneLetterName, residue.oneLetterName)
        && Objects.equals(chain, residue.chain)
        && Objects.equals(insertionCode, residue.insertionCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isMissing,
        standardResidueName,
        modifiedResidueName,
        oneLetterName,
        chain,
        residueNumber,
        insertionCode,
        alpha,
        beta,
        gamma,
        delta,
        epsilon,
        zeta,
        nu0,
        nu1,
        nu2,
        nu3,
        nu4,
        eta,
        theta,
        etaPrim,
        tehtaPrim,
        chi,
        pseudophasePucker);
  }
}
