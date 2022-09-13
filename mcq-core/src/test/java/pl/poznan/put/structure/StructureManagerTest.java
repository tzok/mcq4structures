package pl.poznan.put.structure;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.poznan.put.pdb.analysis.DefaultPdbModel;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.utility.ResourcesHelper;

public class StructureManagerTest {
  private File file1EHZ;
  private File file1EVV;
  private File file100D;
  private PdbModel model1EHZ;
  private PdbModel model1EVV;
  private PdbModel model100D;

  @Before
  public final void setUp() throws URISyntaxException, IOException {
    file1EHZ = ResourcesHelper.loadResourceFile("1EHZ.pdb");
    final List<? extends PdbModel> models1EHZ = StructureManager.loadStructure(file1EHZ);
    assertThat(models1EHZ.size(), is(1));
    model1EHZ = models1EHZ.get(0);

    final List<? extends PdbModel> models1EVV = StructureManager.loadStructure("1EVV");
    assertThat(models1EVV.size(), is(1));
    model1EVV = models1EVV.get(0);
    file1EVV = StructureManager.getFile(model1EVV);

    final List<? extends PdbModel> models100D =
        StructureManager.loadStructure(ResourcesHelper.loadResourceFile("100D.cif"));
    assertThat(models100D.size(), is(1));
    model100D = models100D.get(0);
    file100D = StructureManager.getFile(model100D);
  }

  @After
  public final void tearDown() {
    assertThat(StructureManager.getAllStructures().size(), is(3));
    StructureManager.remove(file1EHZ);
    assertThat(StructureManager.getAllStructures().size(), is(2));
    StructureManager.remove(file1EVV);
    assertThat(StructureManager.getAllStructures().size(), is(1));
    StructureManager.remove(file100D);
    assertThat(StructureManager.getAllStructures().isEmpty(), is(true));

    // this is temporary file downloaded each time in @Before method
    FileUtils.deleteQuietly(file1EVV);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void getFileInvalid() {
    final PdbModel structure = mock(DefaultPdbModel.class);
    StructureManager.getFile(structure);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void getStructureInvalid() {
    StructureManager.getStructure("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public final void getNameInvalid() {
    final PdbModel structure = mock(DefaultPdbModel.class);
    StructureManager.getName(structure);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void loadStructureInvalid() throws Exception {
    StructureManager.loadStructure("invalid");
  }

  @Test
  public final void getFile() {
    final File file = StructureManager.getFile(model1EHZ);
    assertThat(file, is(file1EHZ));
  }

  @Test
  public final void getStructure() {
    final PdbModel structure = StructureManager.getStructure("1EHZ");
    assertThat(structure, is(model1EHZ));
  }

  @Test
  public final void getName() {
    final String name = StructureManager.getName(model1EHZ);
    assertThat(name, is("1EHZ"));
  }

  @Test
  public final void getNameMultiModel() throws Exception {
    final File file = ResourcesHelper.loadResourceFile("2MIY.pdb");
    final List<? extends PdbModel> models = StructureManager.loadStructure(file);
    final int size = models.size();
    assertThat(size, is(18));

    for (int i = 0; i < size; i++) {
      final PdbModel model = models.get(i);
      final String expected = String.format("2MIY.%02d", i + 1);
      final String actual = StructureManager.getName(model);
      assertThat(actual, is(expected));
    }

    StructureManager.remove(file);
  }

  @Test
  public final void getAllStructures() {
    final List<PdbModel> structures = StructureManager.getAllStructures();
    assertThat(
        CollectionUtils.isEqualCollection(
            Arrays.asList(model1EHZ, model1EVV, model100D), structures),
        is(true));
  }

  @Test
  public final void getAllNames() {
    final List<String> names = StructureManager.getAllNames();
    assertThat(
        CollectionUtils.isEqualCollection(Arrays.asList("1EHZ", "1EVV", "100D"), names), is(true));
  }

  @Test
  public final void getNames() {
    final List<String> names = StructureManager.getNames(Arrays.asList(model1EHZ, model1EVV));
    assertThat(CollectionUtils.isEqualCollection(Arrays.asList("1EHZ", "1EVV"), names), is(true));
  }

  @Test
  public final void getModels() {
    final List<PdbModel> models = StructureManager.getModels(file1EHZ);
    assertThat(models.size(), is(1));
    assertThat(models.get(0), is(model1EHZ));
  }
}
