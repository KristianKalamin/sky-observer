package sky_observer.space;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.AltitudeDetector;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;

public class VisibilityCheck {

    public VisibilityCheck() {
    }

    private static final double standardMagnitude = 4.83 - (2.5 * FastMath.log10(0.5));

    public SpaceObject propagate(
            URL resourceUrl,
            String date,
            double observerLat,
            double observerLon,
            String satelliteName,
            String line1,
            String line2) {
        try {
            LocalDateTime observerDate = LocalDateTime.parse(date);
            File resourcePath = Paths.get(resourceUrl.toURI()).toFile();

            // configure Orekit
            if (!resourcePath.exists()) {
                System.err.format(Locale.US, "Failed to find %s folder%n",
                        resourcePath.getAbsolutePath());
                System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                        "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                        "resourcePath.getAbsolutePath()");
                System.exit(1);
            }
            final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
            manager.addProvider(new DirectoryCrawler(resourcePath));

            //  Initial state definition : date, orbit
            AbsoluteDate initialDate = new AbsoluteDate(
                    observerDate.getYear(),
                    observerDate.getMonthValue(),
                    observerDate.getDayOfMonth(),
                    observerDate.getHour(),
                    observerDate.getMinute(),
                    observerDate.getSecond(),
                    TimeScalesFactory.getUTC()
            );

            initialDate = initialDate.shiftedBy(-600); // -10 min
            AbsoluteDate endDate = initialDate.shiftedBy(/*1200*/12000);//-10 +20 min

            final double mu = 3.986004415e+14; // gravitation coefficient
            final Frame inertialFrame = FramesFactory.getEME2000(); // inertial frame for orbit definition

            // TLE tle = new TLE("1 21263U 91032A   20319.89981089 -.00000003 +00000-0 +16436-4 0  9994",
            //         "2 21263 098.5057 325.4736 0012699 232.6162 127.3863 14.25924929534132");
            TLE tle = new TLE(line1, line2);

            if (observerDate.getYear() < tle.getLaunchYear()) {
                System.out.println(observerDate.getYear());
                System.out.println(tle.getLaunchYear());
                return new SpaceObject();
            }

            SpaceObject spaceObject = new SpaceObject();
            TLEPropagator tlePropagator = TLEPropagator.selectExtrapolator(tle);
            PVCoordinates pvCoordinates = tlePropagator.getPVCoordinates(initialDate, inertialFrame);

            final Orbit initialOrbit = new KeplerianOrbit(pvCoordinates, inertialFrame, initialDate, mu);

            // Propagator : consider a simple Keplerian motion (could be more elaborate)
            final Propagator kepler = new KeplerianPropagator(initialOrbit);

            // Earth and frame
            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            final BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING,
                    earthFrame);

            // Station
            final double longitude = FastMath.toRadians(observerLon);//FastMath.toRadians(45.);
            final double latitude = FastMath.toRadians(observerLat);//FastMath.toRadians(25.);
            final double altitude = 0.;
            final GeodeticPoint stationPosition = new GeodeticPoint(latitude, longitude, altitude);
            TopocentricFrame stationFrame = new TopocentricFrame(earth, stationPosition, "observerPosition");

            // Event definition
            final double maxcheck = 60.0;
            final double threshold = 0.001;
            final double elevation = FastMath.toRadians(5.0);

            // Add event to be detected
            kepler.addEventDetector(new ElevationDetector(maxcheck, threshold, stationFrame).
                    withConstantElevation(elevation).
                    withHandler((s, detector, increasing) -> {
                        if (increasing)
                            spaceObject.setStartFlybyTime(new AbsoluteDate(s.getDate(), 0f));
                        else spaceObject.setEndFlybyTime(new AbsoluteDate(s.getDate(), 0f));
                       /* System.out.println(" Visibility on " +
                                detector.getTopocentricFrame().getName() +
                                (increasing ? " begins at " : " ends at ") +
                                s.getDate());*/
                        return increasing ? Action.CONTINUE : Action.STOP;
                    }));

            SpacecraftState finalState = kepler.propagate(endDate);

            if (spaceObject.getStartFlybyTime() == null || spaceObject.getEndFlybyTime() == null) {
                System.out.println("no flyby");
                return new SpaceObject();
            }

            PVCoordinatesProvider sun = CelestialBodyFactory.getSun();
            OneAxisEllipsoid earthEllipsoid = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS, 0.0,
                    FramesFactory.getITRF(IERSConventions.IERS_2010, true));

            EclipseDetector eclipseDetector = new EclipseDetector(sun, 696000000, earthEllipsoid);

            AltitudeDetector altitudeDetector = new AltitudeDetector(maxcheck, threshold, earthEllipsoid);
            double satelliteAltitude = altitudeDetector.g(finalState) / 1000; // km
            double g = eclipseDetector.g(finalState);

            Vector3D sunPosition = sun.getPVCoordinates(finalState.getDate(), earth.getBodyFrame()).getPosition();
            Vector3D groundPointPos = earth.transform(stationPosition);
            Vector3D sunDirection = sunPosition.subtract(groundPointPos);
            double sunElevation = 0.5 * FastMath.PI - Vector3D.angle(sunDirection, stationPosition.getZenith());
            sunElevation = FastMath.toDegrees(sunElevation);

            double mag = 1000;
            if (g > 0 && sunElevation > -18 && sunElevation < -6) {
                Vector3D sunPos = sun.getPVCoordinates(endDate, earthFrame).getPosition();
                Vector3D satellitePos = finalState.getPVCoordinates(earthFrame).getPosition();
                double phaseAngle = FastMath.PI - Vector3D.angle(sunPos.subtract(satellitePos), satellitePos);

                mag = standardMagnitude - 15 + 5 * FastMath.log10(satelliteAltitude) - 2.5 * FastMath.log10(FastMath.sin(phaseAngle) + (FastMath.PI - phaseAngle) * FastMath.cos(phaseAngle));

            }
            spaceObject.setNakedEyeVisibilityMag(mag);
            spaceObject.setSatelliteName(satelliteName);
            return spaceObject;
        } catch (OrekitException | URISyntaxException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
        return null;
    }
}
