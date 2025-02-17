package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dataTypes.Challenge;
import dataTypes.Point;
import dataTypes.Response;
import dataTypes.UserDevicePair;

public class TestUtil {
    /**
     * returns a UserDevicePair preloaded with some challanges
     */
    public static UserDevicePair create_generated_ud_pair() {
        Challenge challenge;
        Response response;
        List<Point> response_points;

        // create a userDeficePair
        UserDevicePair ud_pair = new UserDevicePair(0);

        challenge = generate_challenge();

        for (int i = 0; i < 3; i++) {
            response_points = new ArrayList<Point>();

            // create the response
            for (int j = 0; j < 32; j++) {
                response_points.add(new Point((300 / 31) * j + 100, 100, i, 100, j));
            }

            response = new Response(response_points);
            challenge.addResponse(response);
        }

        // the mu sigma for the responses should be
        // mu : 1
        // sigma : sqrt(2/3)
        ud_pair.addChallenge(challenge);

        return ud_pair;
    }

    /**
     * generates some response points
     */
    public static ArrayList<Point> generate_response_points(double metrics_value){
        ArrayList<Point> response_points  = new ArrayList<Point>();

        // create the response
        int num_points = 10;
        for (int j = 0; j < num_points; j++) {
            Point p = new Point((300 / num_points) * j + 100, 100);

            Point.Metrics[] metrics = Point.Metrics.values();

            for(int i=0; i<metrics.length; i++) {
                p.set_metric(metrics[i], metrics_value);
            }

            response_points.add(p);
        }

        return response_points;
    }

    /**
     * generates a challenge
     */
    public static Challenge generate_challenge(){
        // create a list of challenge points
        List<Point> challenge_points = new ArrayList<Point>();

        // sample points for testing
        challenge_points.add(new Point(100, 100, 0));
        challenge_points.add(new Point(200, 100, 0));
        challenge_points.add(new Point(300, 100, 0));
        challenge_points.add(new Point(400, 100, 0));

        // add the challenge to it which I want to authenticate against
        // create 3 responses to add to this challenge
        return new Challenge(challenge_points, 0);
    }

    /**
     * loads a ud_pair into memory
     * TODO
     */
    public static UserDevicePair create_loaded_ud_pair() {
        UserDevicePair ud_pair = new UserDevicePair(0);

        // TODO

        return ud_pair;
    }

    /**
     * returns the method result
     */
    public static Object runPrivateMethod(Object o, String methodName, Class[] argClasses, Object[] methodParameters) {
        Method method = null;
        Object object = null;

        try {
            method = o.getClass().getDeclaredMethod(methodName, argClasses);

            // set method to accessible
            method.setAccessible(true);

            // invoke the method
            object = method.invoke(o, methodParameters);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return object;
    }
}
