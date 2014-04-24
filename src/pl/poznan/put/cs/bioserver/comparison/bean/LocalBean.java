package pl.poznan.put.cs.bioserver.comparison.bean;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

import pl.poznan.put.cs.bioserver.beans.XMLSerializable;
import pl.poznan.put.cs.bioserver.helper.Exportable;
import pl.poznan.put.cs.bioserver.helper.Helper;
import pl.poznan.put.cs.bioserver.helper.Visualizable;

@XmlRootElement
public abstract class LocalBean extends XMLSerializable implements Exportable,
        Visualizable {
    // public static LocalBean newInstance(Chain c1, Chain c2,
    // List<AngleType> angleNames) throws StructureException {
    // Structure[] s = new Structure[] {
    // new StructureImpl((Chain) c1.clone()),
    // new StructureImpl((Chain) c2.clone()) };
    //
    // StringBuilder builder = new StringBuilder();
    // builder.append(StructureManager.getName(c1.getParent()));
    // builder.append('.');
    // builder.append(c1.getChainID());
    // builder.append(", ");
    // builder.append(StructureManager.getName(c2.getParent()));
    // builder.append('.');
    // builder.append(c2.getChainID());
    // String title = builder.toString();
    //
    // return LocalBean.newInstance(
    // TorsionLocalComparison.compare(s[0], s[1], angleNames), title,
    // angleNames);
    // }
    //
    // public static LocalBean newInstance(Structure left, Structure right,
    // List<Chain> leftChains, List<Chain> rightChains,
    // List<AngleType> list) throws StructureException {
    // Structure l = new StructureImpl();
    // for (Chain c : leftChains) {
    // l.addChain((Chain) c.clone());
    // }
    // Structure r = new StructureImpl();
    // for (Chain c : rightChains) {
    // r.addChain((Chain) c.clone());
    // }
    //
    // String title = StructureManager.getName(left) + ", "
    // + StructureManager.getName(right);
    // return LocalBean.newInstance(
    // TorsionLocalComparison.compare(l, r, list), title, list);
    // }
    //
    // private static LocalBean newInstance(
    // Map<AngleType, List<AngleDifference>> comparison, String title,
    // List<AngleType> angleNames) {
    // Set<AngleType> setAngles = new HashSet<>(angleNames);
    //
    // /*
    // * get a union of all sets of residues for every angle
    // */
    // Set<ResidueNumber> setResidue = new TreeSet<>();
    // for (Entry<AngleType, List<AngleDifference>> entry : comparison
    // .entrySet()) {
    // AngleType angleName = entry.getKey();
    // if (!setAngles.contains(angleName)) {
    // continue;
    // }
    //
    // for (AngleDifference difference : entry.getValue()) {
    // ResidueNumber residue = difference.getResidue();
    // setResidue.add(residue);
    // }
    // }
    //
    // /*
    // * fill a "map[angle, residue] = angle" with values (NaN if missing)
    // */
    // MultiKeyMap<Object, Double> mapAngleResidueDelta = new MultiKeyMap<>();
    // for (Entry<AngleType, List<AngleDifference>> entry : comparison
    // .entrySet()) {
    // AngleType angleName = entry.getKey();
    // if (!setAngles.contains(angleName)) {
    // continue;
    // }
    //
    // for (ResidueNumber residue : setResidue) {
    // mapAngleResidueDelta.put(angleName, residue, Double.NaN);
    // }
    // for (AngleDifference delta : entry.getValue()) {
    // ResidueNumber residue = delta.getResidue();
    // double difference = delta.getDifference();
    // mapAngleResidueDelta.put(angleName, residue, difference);
    // }
    // }
    //
    // /*
    // * read map data into desired format
    // */
    // Map<AngleType, Angle> angles = new LinkedHashMap<>();
    // for (AngleType angleName : comparison.keySet()) {
    // if (!setAngles.contains(angleName)) {
    // continue;
    // }
    //
    // double[] deltas = new double[setResidue.size()];
    // int j = 0;
    // for (ResidueNumber residue : setResidue) {
    // deltas[j] = mapAngleResidueDelta.get(angleName, residue);
    // j++;
    // }
    //
    // Angle angle = new Angle();
    // angle.setName(angleName.getAngleDisplayName());
    // angle.setDeltas(deltas);
    // angles.put(angleName, angle);
    // }
    //
    // List<String> ticks = new ArrayList<>();
    // for (ResidueNumber residue : setResidue) {
    // ticks.add(String.format("%s:%03d", residue.getChainId(),
    // residue.getSeqNum()));
    // }
    // return new LocalBean(angles, Colors.toRGB(), ticks, title);
    // }

    // private Map<AngleType, Angle> angles;
    // private List<RGB> colors;
    // private List<String> ticks;
    private String title;

    //
    // private LocalBean(Map<AngleType, Angle> angles, List<RGB> colors,
    // List<String> ticks, String title) {
    // super();
    // this.angles = angles;
    // this.colors = colors;
    // this.ticks = ticks;
    // this.title = title;
    // }

    public LocalBean(String title) {
        super();
        this.title = title;
    }

    @Override
    public abstract void export(File file) throws IOException;

    // public Map<AngleType, Angle> getAngles() {
    // return angles;
    // }

    // public Map<String, Angle> getAnglesNames() {
    // Map<String, Angle> map = new LinkedHashMap<>();
    // for (Entry<AngleType, Angle> entry : angles.entrySet()) {
    // map.put(entry.getKey().getAngleName(), entry.getValue());
    // }
    // return map;
    // }

    // @XmlElement
    // public void setAnglesNames(Map<String, Angle> map) {
    // Map<AngleType, Angle> result = new LinkedHashMap<>();
    // for (Entry<String, Angle> entry : map.entrySet()) {
    // for (AngleType at : MCQ.USED_ANGLES) {
    // if (at.getAngleName().equals(entry.getKey())) {
    // result.put(at, entry.getValue());
    // break;
    // }
    // }
    // }
    //
    // angles = result;
    // }

    // public List<RGB> getColors() {
    // return colors;
    // }
    //
    // @XmlElement
    // public void setColors(List<RGB> colors) {
    // this.colors = colors;
    // }
    //
    // public List<String> getTicks() {
    // return ticks;
    // }
    //
    // @XmlElement
    // public void setTicks(List<String> ticks) {
    // this.ticks = ticks;
    // }
    //
    // public String getTitle() {
    // return title;
    // }
    //
    // @XmlElement
    // public void setTitle(String title) {
    // this.title = title;
    // }

    @Override
    public File suggestName() {
        String filename = Helper.getExportPrefix();
        filename += "-Local-Distance-";
        filename += title.replace(", ", "-");
        filename += ".csv";
        return new File(filename);
    }

    @Override
    public abstract void visualize();

    // {
    // double[] x = new double[ticks.size()];
    // for (int i = 0; i < ticks.size(); i++) {
    // x[i] = i;
    // }
    //
    // List<Angle> angleArray = new ArrayList<>(getAngles().values());
    // double[][] y = new double[angleArray.size()][];
    // for (int i = 0; i < angleArray.size(); i++) {
    // y[i] = new double[ticks.size()];
    // for (int j = 0; j < ticks.size(); j++) {
    // y[i][j] = angleArray.get(i).getDeltas()[j];
    // }
    // }
    //
    // DefaultXYDataset dataset = new DefaultXYDataset();
    // DefaultXYItemRenderer renderer = new DefaultXYItemRenderer();
    // for (int i = 0; i < y.length; i++) {
    // dataset.addSeries(angleArray.get(i).getName(), new double[][] { x,
    // y[i] });
    // renderer.setSeriesPaint(i, Colors.ALL.get(i + 1));
    // }
    //
    // NumberAxis xAxis = new TorsionAxis(ticks);
    // xAxis.setLabel("ResID");
    // NumberAxis yAxis = new NumberAxis();
    // yAxis.setLabel("Angular distance");
    // yAxis.setRange(0, Math.PI);
    // yAxis.setTickUnit(new NumberTickUnit(Math.PI / 12.0));
    //
    // final ProperFractionFormat format = new ProperFractionFormat();
    // yAxis.setNumberFormatOverride(new NumberFormat() {
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public StringBuffer format(double number, StringBuffer toAppendTo,
    // FieldPosition pos) {
    // assert toAppendTo != null;
    //
    // if (number == 0) {
    // return toAppendTo.append("0");
    // } else if (number == Math.PI) {
    // toAppendTo.append(Constants.UNICODE_PI);
    // toAppendTo.append(" = 180");
    // toAppendTo.append(Constants.UNICODE_DEGREE);
    // return toAppendTo;
    // }
    // format.format(number / Math.PI, toAppendTo, pos);
    // toAppendTo.append(" * ");
    // toAppendTo.append(Constants.UNICODE_PI);
    // toAppendTo.append(" = ");
    // toAppendTo.append(Math.round(Math.toDegrees(number)));
    // toAppendTo.append(Constants.UNICODE_DEGREE);
    // return toAppendTo;
    // }
    //
    // @Override
    // public StringBuffer format(long number, StringBuffer toAppendTo,
    // FieldPosition pos) {
    // return format.format(number, toAppendTo, pos);
    // }
    //
    // @Override
    // public Number parse(String source, ParsePosition parsePosition) {
    // return format.parse(source, parsePosition);
    // }
    // });
    // XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
    //
    // JFrame frame = new JFrame();
    // frame.setLayout(new BorderLayout());
    // frame.add(new ChartPanel(new JFreeChart(plot)));
    //
    // Toolkit toolkit = Toolkit.getDefaultToolkit();
    // Dimension size = toolkit.getScreenSize();
    // frame.setSize(size.width * 2 / 3, size.height * 2 / 3);
    // frame.setLocation(size.width / 6, size.height / 6);
    // frame.setTitle("MCQ4Structures: local distance plot");
    // frame.setVisible(true);
    // }

    @Override
    public abstract void visualize3D();

    // {
    // final List<Angle> angleList = new ArrayList<>(getAngles().values());
    // final int maxX = angleList.size();
    // final int maxY = ticks.size();
    //
    // if (maxX <= 1) {
    // JOptionPane.showMessageDialog(null,
    // "3D plot requires a comparison based on at least "
    // + "two angles", "Warning",
    // JOptionPane.WARNING_MESSAGE);
    // return;
    // }
    //
    // TickLabelMap mapX = new TickLabelMap();
    // for (int i = 0; i < angleList.size(); i++) {
    // mapX.register(i, angleList.get(i).getName());
    // }
    // TickLabelMap mapY = new TickLabelMap();
    // for (int i = 0; i < maxY; i++) {
    // mapY.register(i, ticks.get(i));
    // }
    //
    // Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(new Range(
    // 0, maxX - 1), maxX, new Range(0, maxY), maxY - 1),
    // new Mapper() {
    // @Override
    // public double f(double x, double y) {
    // int i = (int) Math.round(x);
    // int j = (int) Math.round(y);
    //
    // i = Math.max(Math.min(i, maxX - 1), 0);
    // j = Math.max(Math.min(j, maxY - 1), 0);
    // return angleList.get(i).getDeltas()[j];
    // }
    // });
    //
    // surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), 0,
    // (float) Math.PI, new Color(1, 1, 1, .5f)));
    // surface.setFaceDisplayed(true);
    // surface.setWireframeDisplayed(false);
    //
    // Chart chart = new Chart(Quality.Nicest);
    // chart.getScene().getGraph().add(surface);
    //
    // IAxeLayout axeLayout = chart.getAxeLayout();
    // axeLayout.setXTickProvider(new RegularTickProvider(maxX));
    // axeLayout.setXTickRenderer(mapX);
    // axeLayout.setYTickProvider(new SmartTickProvider(maxY));
    // axeLayout.setYTickRenderer(mapY);
    // axeLayout.setZAxeLabel("Angular distance");
    //
    // ChartLauncher.openChart(chart);
    // }

    @Override
    public abstract void visualizeHighQuality();
    // {
    // StringBuilder builder = new StringBuilder();
    // builder.append("[ ");
    // for (AngleType angle : angles.keySet()) {
    // builder.append("'");
    // builder.append(angle.getAngleDisplayName());
    // builder.append("', ");
    // }
    // builder.append(" ]");
    //
    // Map<String, Object> parameters = new HashMap<>();
    // parameters.put("angles", builder.toString());
    //
    // URL resource = MainWindow.class.getResource("/pl/poznan/put/cs/"
    // + "bioserver/external/MatplotlibLocal.xsl");
    // Matplotlib.runXsltAndPython(resource, this, parameters);
    // }
}
