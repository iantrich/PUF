package dataTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents challenge which contains the pattern of points for the
 * corresponding challenge, responses that a user will create for said
 * challenge, and that user's profile which will be correlated with this
 * challenge
 */
public class Challenge implements Serializable {
    // use a fixed distance between normalizing points
    final static boolean USE_FIXED_NORMALIZING_POINT_DISTANCE = false;
    // define the fixed normalizing point difference
    final static double FIXED_NORMALIZING_POINT_DISTANCE = 25.0; //25.0;

    // if true, always use NORMALIZED_ELEMENTS_DEFAULT
    final static boolean USE_FIXED_NORMALIZING_POINT_NUMBER = true;
    // number of elements in normalized list default value.
    //TODO modified
    //final static int NORMALIZED_ELEMENTS_DEFAULT = 32;
    final static int NORMALIZED_ELEMENTS_DEFAULT = Response.RESPONSE_BITS;

    // threshold for determining if response has enough motion events
    final static int NORMALIZED_THRESHOLD = 20;

    // number of elements in the normalized list
    private int normalized_elements;

    // a list of number of points in each response added to the challenge
    // (used for computing mu/sigma of number of MotionEvents
    private ArrayList<Double> motion_event_counts;

    // Pattern of points that create the challenge
    private List<Point> challengePattern;

    // pattern of points to normalize responses to
    public List<Point> normalizingPoints;

    // Unique identifier to distinguish which challenge this is
    private long challengeID;

    // List of NORMALIZED responses, each representing one response by the user
    // for this challenge
    private List<Response> responses;

    private boolean isChallengeHorizontal;

    // Profile associated with this challenge
    private Profile profile;

    // time_length values for the responses
    private ArrayList<Double> time_lengths;

    // changes
    public Challenge(List<Point> challengePattern, long challengeID) {
        this(challengePattern, challengeID, NORMALIZED_ELEMENTS_DEFAULT);
    }

    /**
     * allows for parameterization of the number of normalized elements
     *
     * @param challengePattern
     * @param challengeID
     * @param normalizationPoints
     */
    public Challenge(List<Point> challengePattern, long challengeID, int normalizationPoints) {
        this.challengePattern = challengePattern;
        this.challengeID = challengeID;
        this.normalized_elements = normalizationPoints;

        responses = new ArrayList<Response>();
        this.time_lengths = new ArrayList<Double>();
        profile = null;
        motion_event_counts = new ArrayList<Double>();
    }

    // Adds normalized response to the list of Responses
    public void addResponse(Response response) {
        // add the time length of the response to the list
        this.time_lengths.add(response.getTimeLength());

        // cause any duplicate points in the origional response to be filtered out'
        int removed_count = response.remove_duplicates();

        // checking to see if any points were removed. This is included for testing purposes
        if( removed_count > 0){
            //System.out.println("Duplicate points removed:\t" + removed_count);
        }

        // If first response added, use it as baseline for number of
        // normalization points,
        // then calculated normalizing points with
        if (responses.size() <= 0) {
            this.normalized_elements = response.getOrigionalResponse().size();// - 2;

            // compute the list of points used to normalize the responses to
            // this
            // challenge
            // normalizingPoints = computeNormalizingPoints(x_dist, y_dist);

            // euclidean distance
            this.normalizingPoints = computeNormalizingPoints(response);
        }

        else {
        }

        // before normalizing response, add length of the response to list of
        // motion_event_counts
        motion_event_counts.add(new Double(response.getMotionEvenCount()));

        // normalize the response before it is added to the challenge
        // System.out.println(isChallengeHorizontal);
        response.normalize(normalizingPoints);

        responses.add(response);

        // profile is now invalid and needs to be re-computed
        this.profile = null;
    }

    // Creates a profile associate with this challenge with NORMALIZED responses
    public Profile getProfile() {
        // if the profile hasn't been created, create the profile
        if (profile == null) {
            // all properties of the points are computed when this is created
            profile = new Profile(responses, time_lengths, motion_event_counts);
        }

        return profile;
    }

    public int getNormalizedElementsCount() {
        return normalized_elements;
    }

    public void setChallengeID(long challengeID) {
        this.challengeID = challengeID;
    }

    public long getChallengeID() {
        return challengeID;
    }

    public List<Double> getmotion_event_counts() {
        return motion_event_counts;
    }

    public List<Point> getChallengePattern() {
        return challengePattern;
    }

    public List<Point> getNormalizingPoints() {return normalizingPoints; }

    public List<Response> getResponsePattern() {
        return responses;
    }

    /**
     * compute list of normalizing points using euclidean distance.
     * this is based on the first response points.
     */
    private List<Point> computeNormalizingPoints(Response response) {
        List<Point> norm_points = new ArrayList<Point>();
        List<Point> response_points = response.getOrigionalResponse();

        int number_np = 0;
        if(USE_FIXED_NORMALIZING_POINT_NUMBER){
            number_np = NORMALIZED_ELEMENTS_DEFAULT;
        }

        // compute the distance between each normalizing point ( N-1 segments to split d into)
        double total_response_distance = computeResponseLength(response_points);
        //double inter_point_distance = total_response_distance / (response_points.size() - 1);
        double inter_point_distance = total_response_distance / (number_np - 1);

        // determine the number of normalization points
        if(USE_FIXED_NORMALIZING_POINT_DISTANCE){
            inter_point_distance = FIXED_NORMALIZING_POINT_DISTANCE;

            // would rather have one too few points than one to many
            // this is because i'm thinking of the fixed distance
            // as a minimum
            // if I wanted to think of it as a maximum,
            // I could do math.ceil instead of floor.
            number_np = (int)Math.ceil(total_response_distance / inter_point_distance);
        }else{
            if(!USE_FIXED_NORMALIZING_POINT_NUMBER) {
                number_np = response_points.size();
                inter_point_distance = total_response_distance / (number_np - 1);
            }
        }

        // first point in the list is the first point in the response
        norm_points.add(response_points.get(0));

        // for n-2 oher points, for a total of n-1
        for(int i=1; i<number_np-1; i++){
            // determine the distance along the trace for this NP
            double np_distance = inter_point_distance * i;

            // determine the left neighbor and
            // the distance along the trace at which left neighbor lies
            int before_neighbor_index = 0;
            double before_neighbor_distance = 0.0;
            double after_neighbor_distance = 0.0;
            for (int j = 1; j < response_points.size(); j++) {
                before_neighbor_index= j-1;
                before_neighbor_distance = after_neighbor_distance;

                after_neighbor_distance += computeEuclideanDistance(response_points.get(j), response_points.get(j - 1));

                // if this last distance has pushed me over np_distance, exit
                // before_neighbor_index should be the left neighbor
                // before_neighbor_distance is the distance along the response of left neighbor
                if(after_neighbor_distance > np_distance) break;
            }

            // place the NP on the line between before_neighbor and after_neighbor
            // distance greater than before_neighbor
            //
            // determine the distance of the NP along the line (guarenteed to be positive)
            Point before_neighbor = response_points.get(before_neighbor_index);
            Point after_neighbor = response_points.get(before_neighbor_index+1);
            double np_line_distance = np_distance - before_neighbor_distance;

            // move np_line_distance in the direction from before_neighbor to after_neighbor
            double neighbor_x_distance = after_neighbor.getX() - before_neighbor.getX();
            double neighbor_y_distance = after_neighbor.getY() - before_neighbor.getY();
            double neighbor_euclidean_distance = computeEuclideanDistance(before_neighbor, after_neighbor);

            // compute the ratio of the total distance between the points
            // the normalization point covers
            double np_distance_coverage_ratio = np_line_distance / neighbor_euclidean_distance;

            // now compute the x,y distance the normalization point should be from the left neighbor
            double np_x_distance = np_distance_coverage_ratio * neighbor_x_distance;
            double np_y_distance = np_distance_coverage_ratio * neighbor_y_distance;

            // compute the absolute x,y points of the normalization point
            double np_x = before_neighbor.getX() + np_x_distance;
            double np_y = before_neighbor.getY() + np_y_distance;

            // add the NP to the list
            norm_points.add(new Point(np_x, np_y));
        }

        // the last normalizing point is the last point of the response
        norm_points.add(response_points.get(response_points.size()-1));

        return norm_points;
    }

    private List<Point> computeNormalizingPoints_old(Response response) {
        List<Point> norm_points = new ArrayList<Point>();
        List<Point> response_points = response.getOrigionalResponse();

        // compute the distance between each normalizing point ( N-1 segments to split d into)
        // TODO make sure length is being computed correctly
        double distance = computeResponseLength(response_points) / (response_points.size() - 1);

        //System.out.println("distance:" + distance);

        // first point in the list is the first point in the response
        norm_points.add(response_points.get(0));

        // choose all the points in the middle (N-2) of them
        // for each norma
        // normalization point to find
        // i is normalization points
        // j is response points
        int j = 1;
        for (int i = 0; i < response_points.size() - 2; i++) {
            // k keeps track of the number of indexs the next point is away from the current point
            int k = 0;

            // add prev_remaining_distance to distance to avoid having the distance from
            // left neighbor to the previous normalization point be double counted
            double remaining_distance = distance; //- right_neighbor_distance; //+ prev_remaining_distance;

            // determine the closest left neighbor (j + k - 1) and
            // the distance of the normalization point from this neighbor

            //System.out.println("distance:" + distance);

            // TODO left and right neighbor are found correctly in some instances, and incorrectly in others
            // TODO keep the cumuplate distance
            double covered_distance = computeEuclideanDistance(norm_points.get(i), response_points.get(j));

            while (covered_distance < distance) {
                k++;

                remaining_distance = distance - covered_distance;

                covered_distance += computeEuclideanDistance(response_points.get(j + k -1), response_points.get(j+k));
                // if it is the first point, take the differance from the previous normalization point
//                if(k == 0) {
//                    //remaining_distance -= computeEuclideanDistance(norm_points.get(i), response_points.get(j));
//                    remaining_distance -= covered_distance;
//                } else {
//                    remaining_distance -= computeEuclideanDistance(norm_points.get(j + k - 1), response_points.get(j + k));
//                }

                //System.out.println("in loop remaining distance" + remaining_distance);

                //k++;
            }

            // now we know the point closes to the left of the normalization point in the response.
            j += k;

            //System.out.print("left point:" + ( j-1 ) + "\trem_dist:");
            //System.out.println(remaining_distance);

            // because tan returns a value between -pi/2 and pi/2, cos will never be negative
            // we need to presevere the x direction we are travling
            double x_differance = (response_points.get(j).getX() - response_points.get(j - 1).getX());
            double x_sine = 1;

            // check that the differance isn't equal to zero to ensure no divide by 0 error
            if(!(x_differance==0)){
                // this will make x_sine +1 or -1 depending on the sine
                x_sine = x_differance / Math.abs(x_differance);
            }

            // now normalization point is between j and j-1
            double theta = Math.atan((response_points.get(j).getY() - response_points.get(j - 1).getY()) /
                    x_differance);

            //System.out.println("theta:"+theta);

            // compute the appropriate x,y coordinates for the point
            int norm_point_x = (int) (response_points.get(j - 1).getX() + remaining_distance * Math.cos(theta) * x_sine);
            int norm_point_y = (int) (response_points.get(j - 1).getY() + remaining_distance * Math.sin(theta) * x_sine);

            // at the point to the normalization points list
            norm_points.add(new Point(norm_point_x, norm_point_y, 0));
        }

        // last point in the list is the last point in the response
        norm_points.add(response_points.get(response_points.size() - 1));

        //System.out.println(response_points);
        //System.out.println(norm_points);

        return norm_points;
    }

    /**
     * compute the euclidean length of a point list
     */
    public static double computeResponseLength(List<Point> points) {
        double length = 0.0;

        for (int i = 1; i < points.size(); i++) {
            length += computeEuclideanDistance(points.get(i), points.get(i - 1));
        }

        return length;
    }

    /**
     * compute the euclidean distance between two points
     */
    public static double computeEuclideanDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.getX() - p2.getX()), 2) +
                Math.pow((p1.getY() - p2.getY()), 2));
    }

    /**
     * computes a list of points to be used in normalization of responses
     */
    private List<Point> computeNormalizingPoints(double x_dist, double y_dist) {
        List<Point> x_y_list;

        if (isChallengeHorizontal) {
            // normalize with respect to x
            x_y_list = computeHorizontalPointsAlongChallenge(x_dist);
        } else {
            // normalize with respect to y
            x_y_list = computeVerticalPointsAlongChallenge(y_dist);
        }

        return x_y_list;
    }

    /**
     * return a list of NORMALIZED_ELEMENTS number of points horizontally along
     * the challenge. We will normalize with respect to these points.
     */
    private List<Point> computeHorizontalPointsAlongChallenge(double x_dist) {
        ArrayList<Point> points = new ArrayList<Point>();

        // amount of x separation for each point
        double point_increment = x_dist / (this.normalized_elements - 1);
        Point next_point = null;
        Point min_challenge_point = min_challenge_point_x();

        for (int i = 0; i < this.normalized_elements; i++) {
            // do something different for the first iteration
            if (points.size() == 0) {
                // use the x,y values of the first point
                next_point = new Point(min_challenge_point);
            } else {
                // add point increment to the x value while staying at the same
                // y value
                next_point = new Point(points.get(i - 1).getX() + point_increment, points.get(i - 1).getY(),
                        points.get(i - 1).getPressure());
            }

            points.add(next_point);
        }

        return points;

    }

    /**
     * return a list of NORMALIZED_ELEMENTS number of points vertically along
     * the challenge. We will normalize with respect to these points.
     */
    private List<Point> computeVerticalPointsAlongChallenge(double y_dist) {
        ArrayList<Point> points = new ArrayList<Point>();

        // amount of y separation for each point
        double point_increment = y_dist / (this.normalized_elements - 1);
        Point next_point = null;
        Point min_challenge_point = min_challenge_point_y();

        for (int i = 0; i < this.normalized_elements; i++) {
            // do something different for the first iteration
            if (points.size() == 0) {
                // use the x,y values of the minimum point
                next_point = new Point(min_challenge_point);
            } else {
                // add point increment to the y value while staying at the same
                // x value
                next_point = new Point(points.get(i - 1).getX(), points.get(i - 1).getY() + point_increment,
                        points.get(i - 1).getPressure());
            }

            points.add(next_point);
        }

        return points;
    }

    /**
     * computes the y distance covered by this challenge
     *
     * @return
     */
    private double computeChallengeYDistance() {
        if (challengePattern == null) {
            return 0.0;
        }

        Point min = this.challengePattern.get(0);
        Point max = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getY() < min.getY()) {
                min = challenge_point;
            }

            if (challenge_point.getY() > max.getY()) {
                max = challenge_point;
            }
        }

        return max.getY() - min.getY();
    }

    /**
     * computes the x distance covered by this challenge
     *
     * @return
     */
    private double computeChallengeXDistance() {
        if (challengePattern == null) {
            return 0.0;
        }

        Point min = this.challengePattern.get(0);
        Point max = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getX() < min.getX()) {
                min = challenge_point;
            }

            if (challenge_point.getX() > max.getX()) {
                max = challenge_point;
            }
        }

        return max.getX() - min.getX();
    }

    /**
     * get the max challenge point
     */
    private Point max_challenge_point_x() {
        if (challengePattern == null) {
            return null;
        }

        Point min = this.challengePattern.get(0);
        Point max = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getX() > max.getX()) {
                max = challenge_point;
            }
        }

        return max;
    }

    /**
     * get the min challenge point
     */
    private Point min_challenge_point_x() {
        if (challengePattern == null) {
            return null;
        }

        Point min = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getX() < min.getX()) {
                min = challenge_point;
            }
        }

        return min;
    }

    /**
     * get the max challenge point
     */
    private Point max_challenge_point_y() {
        if (challengePattern == null) {
            return null;
        }

        Point max = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getY() > max.getY()) {
                max = challenge_point;
            }
        }

        return max;
    }

    /**
     * get the min challenge point
     */
    private Point min_challenge_point_y() {
        if (challengePattern == null) {
            return null;
        }

        Point min = this.challengePattern.get(0);

        for (Point challenge_point : this.challengePattern) {
            if (challenge_point.getY() < min.getY()) {
                min = challenge_point;
            }
        }

        return min;
    }
}