import com.fasterxml.uuid.Generators;
import org.geotools.api.filter.identity.FeatureId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertTest {
    public static Network containerNetwork = Network.newNetwork();
    public static GenericContainer<?> POSTGIS;
    public static GenericContainer<?> GEOSERVER;
    private final static String TYPE_NAME = "bis:test";
    private static final List<String> VERSIONS = List.of("1.1.0", "2.0.0");
    private static final Geometry GEOMETRY = getGeometry();


    @BeforeAll
    static void init() {
        POSTGIS = new GenericContainer<>("postgis/postgis")
                .withExposedPorts(5432)
                .withEnv("POSTGRES_PASSWORD", "postgres")
                .withEnv("POSTGRES_USER", "postgres")
                .withCopyFileToContainer(MountableFile.forHostPath("./create_db.sql"), "/docker-entrypoint-initdb.d/")
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("db"))
                .withNetwork(containerNetwork);

        GEOSERVER = new GenericContainer<>("docker.osgeo.org/geoserver:2.26.0")
                .withExposedPorts(8080)
                .withFileSystemBind("./geoserver_data", "/opt/geoserver_data")
                .withNetwork(containerNetwork)
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName("geoserver-test"))
                .waitingFor(
                        Wait.forHttp("/geoserver/")
                                .forPort(8080)
                                .forStatusCode(200)
                                .withReadTimeout(Duration.ofSeconds(5))
                );

        POSTGIS.start();
        GEOSERVER.start();
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForInsert")
    void insertGeometry(String wfsVersion) {
        String insertId = "test." + Generators.timeBasedEpochGenerator().generate();
        FeatureId featureId = GeotoolsUtils.createGeometry(GEOSERVER.getFirstMappedPort(), TYPE_NAME, GEOMETRY, insertId, wfsVersion);
        assertEquals(featureId.getID(), insertId);
    }

    public static Stream<Arguments> getArgumentsForInsert() {
        return VERSIONS.stream().map(Arguments::of);
    }

    public static Geometry getGeometry() {
        PrecisionModel pm = new PrecisionModel();
        GeometryFactory gf = new GeometryFactory(pm, 4326);
        CoordinateArraySequence cas =
                new CoordinateArraySequence(new Coordinate[]{
                        new Coordinate(30.262776196255125, 59.93954262728195),
                        new Coordinate(30.262907623301622, 59.93958105131431),
                        new Coordinate(30.262941029017526, 59.9395526213441),
                        new Coordinate(30.262956789606722, 59.93953962130284),
                        new Coordinate(30.263027460562363, 59.939488541678394),
                        new Coordinate(30.262897264629483, 59.93944742258056),
                        new Coordinate(30.262776196255125, 59.93954262728195)
                });

        LinearRing lr = new LinearRing(cas, gf);
        return new Polygon(lr, null, gf);
    }
}