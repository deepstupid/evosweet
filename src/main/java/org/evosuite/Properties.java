/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central property repository. All global parameters of EvoSuite should be
 * declared as fields here, using the appropriate annotation. Access is possible
 * directly via the fields, or with getter/setter methods.
 * 
 * @author Gordon Fraser
 */
public class Properties {

	private final static Logger logger = LoggerFactory.getLogger(Properties.class);

	/**
	 * Parameters are fields of the Properties class, annotated with this
	 * annotation. The key parameter is used to identify values in property
	 * files or on the command line, the group is used in the config file or
	 * input plugins to organize parameters, and the description is also
	 * displayed there.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Parameter {
		String key();

		String group() default "Experimental";

		String description();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IntValue {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface LongValue {
		long min() default Long.MIN_VALUE;

		long max() default Long.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface DoubleValue {
		double min() default -(Double.MAX_VALUE - 1); // FIXXME: Check

		double max() default Double.MAX_VALUE;
	}

	// ---------------------------------------------------------------
	// Test sequence creation
	/** Constant <code>TEST_EXCLUDES="test.excludes"</code> */
	@Parameter(key = "test_excludes", group = "Test Creation", description = "File containing methods that should not be used in testing")
	public static String TEST_EXCLUDES = "test.excludes";

	/** Constant <code>TEST_INCLUDES="test.includes"</code> */
	@Parameter(key = "test_includes", group = "Test Creation", description = "File containing methods that should be included in testing")
	public static String TEST_INCLUDES = "test.includes";

	/** Constant <code>EVOSUITE_USE_UISPEC=false</code> */
	@Parameter(key = "evosuite_use_uispec", group = "Test Creation", description = "If set to true EvoSuite test generation inits UISpec in order to avoid display of UI")
	public static boolean EVOSUITE_USE_UISPEC = false;

	/** Constant <code>MAKE_ACCESSIBLE=true</code> */
	@Parameter(key = "make_accessible", group = "TestCreation", description = "Change default package rights to public package rights (?)")
	public static boolean MAKE_ACCESSIBLE = true;

	/** Constant <code>STRING_REPLACEMENT=true</code> */
	@Parameter(key = "string_replacement", group = "Test Creation", description = "Replace string.equals with levenshtein distance")
	public static boolean STRING_REPLACEMENT = true;

	/** Constant <code>STATIC_HACK=false</code> */
	@Parameter(key = "static_hack", group = "Test Creation", description = "Call static constructors after each test execution")
	public static boolean STATIC_HACK = false;

	/**
	 * TODO: this option is off by default because still experimental and not
	 * fully tested
	 */
	@Parameter(key = "test_carving", group = "Test Creation", description = "Enable test carving")
	public static boolean TEST_CARVING = false;

	/** Constant <code>NULL_PROBABILITY=0.1</code> */
	@Parameter(key = "null_probability", group = "Test Creation", description = "Probability to use null instead of constructing an object")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double NULL_PROBABILITY = 0.1;

	/** Constant <code>OBJECT_REUSE_PROBABILITY=0.9</code> */
	@Parameter(key = "object_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing reference, if available")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double OBJECT_REUSE_PROBABILITY = 0.9;

	/** Constant <code>PRIMITIVE_REUSE_PROBABILITY=0.5</code> */
	@Parameter(key = "primitive_reuse_probability", group = "Test Creation", description = "Probability to reuse an existing primitive, if available")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double PRIMITIVE_REUSE_PROBABILITY = 0.5;

	/** Constant <code>PRIMITIVE_POOL=0.5</code> */
	@Parameter(key = "primitive_pool", group = "Test Creation", description = "Probability to use a primitive from the pool rather than a random value")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double PRIMITIVE_POOL = 0.5;

	/** Constant <code>DYNAMIC_POOL=0.5</code> */
	@Parameter(key = "dynamic_pool", group = "Test Creation", description = "Probability to use a primitive from the dynamic pool rather than a random value")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double DYNAMIC_POOL = 0.5; //1d / 3d;

	/** Constant <code>DYNAMIC_POOL_SIZE=50</code> */
	@Parameter(key = "dynamic_pool_size", group = "Test Creation", description = "Number of dynamic constants to keep")
	public static int DYNAMIC_POOL_SIZE = 50;

	@Parameter(key = "p_special_type_call", group = "Test Creation", description = "Probability of using a non-standard call on a special case (collection/numeric)")
	public static double P_SPECIAL_TYPE_CALL = 0.05;

	/** Constant <code>OBJECT_POOL=0.0</code> */
	@Parameter(key = "object_pool", group = "Test Creation", description = "Probability to use a predefined sequence from the pool rather than a random generator")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double OBJECT_POOL = 0.0;

	@Parameter(key = "seed_types", group = "Test Creation", description = "Use type information gathered from casts to instantiate generics")
	public static boolean SEED_TYPES = true;

	@Parameter(key = "max_generic_depth", group = "Test Creation", description = "Maximum level of nesting for generic types")
	public static int MAX_GENERIC_DEPTH = 1;

	/** Constant <code>STRING_LENGTH=20</code> */
	@Parameter(key = "string_length", group = "Test Creation", description = "Maximum length of randomly generated strings")
	public static int STRING_LENGTH = 20;

	/** Constant <code>EPSILON=0.001</code> */
	@Parameter(key = "epsilon", group = "Test Creation", description = "Epsilon for floats in local search")
	@Deprecated
	// does not seem to be used anywhere
	public static double EPSILON = 0.001;

	/** Constant <code>MAX_INT=2048</code> */
	@Parameter(key = "max_int", group = "Test Creation", description = "Maximum size of randomly generated integers (minimum range = -1 * max)")
	public static int MAX_INT = 2048;

	/** Constant <code>RESTRICT_POOL=false</code> */
	@Parameter(key = "restrict_pool", group = "Test Creation", description = "Prohibit integers in the pool greater than max_int")
	public static boolean RESTRICT_POOL = false;

	/** Constant <code>MAX_DELTA=20</code> */
	@Parameter(key = "max_delta", group = "Test Creation", description = "Maximum size of delta for numbers during mutation")
	public static int MAX_DELTA = 20;

	/** Constant <code>RANDOM_PERTURBATION=0.2</code> */
	@Parameter(key = "random_perturbation", group = "Test Creation", description = "Probability to replace a primitive with a random new value rather than adding a delta")
	public static double RANDOM_PERTURBATION = 0.2;

	/** Constant <code>MAX_ARRAY=10</code> */
	@Parameter(key = "max_array", group = "Test Creation", description = "Maximum length of randomly generated arrays")
	public static int MAX_ARRAY = 10;

	/** Constant <code>MAX_ATTEMPTS=1000</code> */
	@Parameter(key = "max_attempts", group = "Test Creation", description = "Number of attempts when generating an object before giving up")
	public static int MAX_ATTEMPTS = 1000;

	/** Constant <code>MAX_RECURSION=10</code> */
	@Parameter(key = "max_recursion", group = "Test Creation", description = "Recursion depth when trying to create objects")
	public static int MAX_RECURSION = 10;

	/** Constant <code>MAX_LENGTH=0</code> */
	@Parameter(key = "max_length", group = "Test Creation", description = "Maximum length of test suites (0 = no check)")
	public static int MAX_LENGTH = 0;

	/** Constant <code>MAX_SIZE=100</code> */
	@Parameter(key = "max_size", group = "Test Creation", description = "Maximum number of test cases in a test suite")
	public static int MAX_SIZE = 100;

	/** Constant <code>NUM_TESTS=2</code> */
	@Parameter(key = "num_tests", group = "Test Creation", description = "Number of tests in initial test suites")
	public static int NUM_TESTS = 2;

	@Parameter(key = "num_random_tests", group = "Test Creation", description = "Number of random tests")
	public static int NUM_RANDOM_TESTS = 20;

	/** Constant <code>MIN_INITIAL_TESTS=1</code> */
	@Parameter(key = "min_initial_tests", group = "Test Creation", description = "Minimum number of tests in initial test suites")
	public static int MIN_INITIAL_TESTS = 1;

	/** Constant <code>MAX_INITIAL_TESTS=10</code> */
	@Parameter(key = "max_initial_tests", group = "Test Creation", description = "Maximum number of tests in initial test suites")
	public static int MAX_INITIAL_TESTS = 10;

	/** Constant <code>USE_DEPRECATED=false</code> */
	@Parameter(key = "use_deprecated", group = "Test Creation", description = "Include deprecated methods in tests")
	public static boolean USE_DEPRECATED = false;

	/** Constant <code>INSERTION_SCORE_UUT=1</code> */
	@Parameter(key = "insertion_score_uut", group = "Test Creation", description = "Score for selection of insertion of UUT calls")
	public static int INSERTION_SCORE_UUT = 1;

	/** Constant <code>INSERTION_SCORE_OBJECT=1</code> */
	@Parameter(key = "insertion_score_object", group = "Test Creation", description = "Score for selection of insertion of call on existing object")
	public static int INSERTION_SCORE_OBJECT = 1;

	/** Constant <code>INSERTION_SCORE_PARAMETER=1</code> */
	@Parameter(key = "insertion_score_parameter", group = "Test Creation", description = "Score for selection of insertion call with existing object")
	public static int INSERTION_SCORE_PARAMETER = 1;

	// ---------------------------------------------------------------
	// Search algorithm
	public enum Algorithm {
		STANDARDGA, STEADYSTATEGA, ONEPLUSONEEA, MUPLUSLAMBDAGA, RANDOM
	}

	/** Constant <code>ALGORITHM</code> */
	@Parameter(key = "algorithm", group = "Search Algorithm", description = "Search algorithm")
	public static Algorithm ALGORITHM = Algorithm.STEADYSTATEGA;

	/** Constant <code>CHECK_BEST_LENGTH=true</code> */
	@Parameter(key = "check_best_length", group = "Search Algorithm", description = "Check length against length of best individual")
	public static boolean CHECK_BEST_LENGTH = true;

	/** Constant <code>CHECK_PARENTS_LENGTH=false</code> */
	@Parameter(key = "check_parents_length", group = "Search Algorithm", description = "Check length against length of parents")
	public static boolean CHECK_PARENTS_LENGTH = false; // note, based on STVR experiments

	// @Parameter(key = "check_rank_length", group = "Search Algorithm", description = "Use length in rank selection")
	// public static boolean CHECK_RANK_LENGTH = false;

	/** Constant <code>PARENT_CHECK=true</code> */
	@Parameter(key = "parent_check", group = "Search Algorithm", description = "Check against parents in Mu+Lambda algorithm")
	public static boolean PARENT_CHECK = true;

	/** Constant <code>CHECK_MAX_LENGTH=true</code> */
	@Parameter(key = "check_max_length", group = "Search Algorithm", description = "Check length against fixed maximum")
	public static boolean CHECK_MAX_LENGTH = true;

	/** Constant <code>DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS=0</code> */
	@Parameter(key = "dse_constraint_solver_timeout_millis", group = "Search Algorithm", description = "Maximum number of solving time for Constraint solver in milliseconds")
	public static long DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = 0;

	/** Constant <code>DSE_RANK_BRANCH_CONDITIONS=false</code> */
	@Parameter(key = "dse_rank_branch_conditions", group = "Search Algorithm", description = "Rank branch conditions")
	public static boolean DSE_RANK_BRANCH_CONDITIONS = false;

	/** Constant <code>DSE_NEGATE_ALL_CONDITIONS=false</code> */
	@Parameter(key = "dse_negate_all_conditions", group = "Search Algorithm", description = "Negate all branch conditions in the path condition (covered or not)")
	public static boolean DSE_NEGATE_ALL_CONDITIONS = false;

	/** Constant <code>DSE_RATE=-1</code> */
	@Parameter(key = "dse_rate", group = "Search Algorithm", description = "Apply DSE at every X generation")
	public static int DSE_RATE = -1;

	@Parameter(key = "dse_adaptive_probability", group = "Search Algorithm", description = "Apply DSE with a certain, adaptive, probability")
	public static double DSE_ADAPTIVE_PROBABILITY = 0.0;

	@Parameter(key = "dse_adaptive_rate", group = "Search Algorithm", description = "If successful, increase DSE probability by factor...")
	public static double DSE_ADAPTIVE_RATE = 2;

	/** Constant <code>DSE_CONSTRAINT_LENGTH=100000</code> */
	@Parameter(key = "dse_constraint_length", group = "Search Algorithm", description = "Maximal length of the constraints in DSE")
	public static int DSE_CONSTRAINT_LENGTH = 100000;

	@Parameter(key = "dse_constant_probability", group = "Search Algorithm", description = "Probability with which to use constants from the constraints when resetting variables during search")
	public static double DSE_CONSTANT_PROBABILITY = 0.5;

	public enum DSEBudgetType {
		INDIVIDUALS, TIME, BEST_TIME
	}

	/** Constant <code>DSE_BUDGET_TYPE</code> */
	@Parameter(key = "dse_budget_type", group = "Search Algorithm", description = "Interpretation of dse_budget property")
	public static DSEBudgetType DSE_BUDGET_TYPE = DSEBudgetType.INDIVIDUALS;

	/** Constant <code>DSE_BUDGET=1</code> */
	@Parameter(key = "dse_budget", group = "Search Algorithm", description = "Milliseconds allowed for dse local search")
	@IntValue(min = 0)
	public static long DSE_BUDGET = 1;

	/** Constant <code>DSE_VARIABLE_RESETS=1</code> */
	@Parameter(key = "dse_variable_resets", group = "Search Algorithm", description = "Times DSE resets the int and real variables with random values")
	public static int DSE_VARIABLE_RESETS = 2;

	/** Constant <code>LOCAL_SEARCH_RATE=-1</code> */
	@Parameter(key = "local_search_rate", group = "Search Algorithm", description = "Apply local search at every X generation")
	public static int LOCAL_SEARCH_RATE = -1;

	public enum AdaptiveLocalSearchTarget {
		OFF, BEST, ALL
	}

	@Parameter(key = "adaptive_local_search", group = "Search Algorithm", description = "Apply adaptive local search")
	public static AdaptiveLocalSearchTarget ADAPTIVE_LOCAL_SEARCH = AdaptiveLocalSearchTarget.OFF;

	@Parameter(key = "adaptive_local_search_dse", group = "Search Algorithm", description = "Use DSE on entire test suite in local search")
	public static boolean ADAPTIVE_LOCAL_SEARCH_DSE = false;

	@Parameter(key = "dse_keep_all_tests", group = "Search Algorithm", description = "Keep tests even if they do not increase fitness")
	public static boolean DSE_KEEP_ALL_TESTS = false;

	/** Constant <code>LOCAL_SEARCH_BUDGET=100</code> */
	@Parameter(key = "local_search_budget", group = "Search Algorithm", description = "Maximum attempts at improving individuals per local search")
	public static long LOCAL_SEARCH_BUDGET = 100;

	public enum LocalSearchBudgetType {
		STATEMENTS, TIME, INDIVIDUALS
	}

	/** Constant <code>LOCAL_SEARCH_BUDGET_TYPE</code> */
	@Parameter(key = "local_search_budget_type", group = "Search Algorithm", description = "Interpretation of local_search_budget")
	public static LocalSearchBudgetType LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.STATEMENTS;

	/** Constant <code>LOCAL_SEARCH_PROBES=10</code> */
	@Parameter(key = "local_search_probes", group = "Search Algorithm", description = "How many mutations to apply to a string to check whether it improves coverage")
	public static int LOCAL_SEARCH_PROBES = 10;

	@Parameter(key = "local_search_primitives", group = "Search Algorithm", description = "Perform local search on primitive values")
	public static boolean LOCAL_SEARCH_PRIMITIVES = true;

	@Parameter(key = "local_search_arrays", group = "Search Algorithm", description = "Perform local search on array statements")
	public static boolean LOCAL_SEARCH_ARRAYS = true;

	@Parameter(key = "local_search_references", group = "Search Algorithm", description = "Perform local search on reference types")
	public static boolean LOCAL_SEARCH_REFERENCES = true;

	/** Constant <code>CROSSOVER_RATE=0.75</code> */
	@Parameter(key = "crossover_rate", group = "Search Algorithm", description = "Probability of crossover")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CROSSOVER_RATE = 0.75;

	/** Constant <code>NUMBER_OF_MUTATIONS=1</code> */
	@Parameter(key = "number_of_mutations", group = "Search Algorithm", description = "Number of single mutations applied on an individual when a mutation event occurs")
	public static int NUMBER_OF_MUTATIONS = 1;

	/** Constant <code>P_TEST_INSERTION=0.1</code> */
	@Parameter(key = "p_test_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new test in a test suite")
	public static double P_TEST_INSERTION = 0.1;

	/** Constant <code>P_STATEMENT_INSERTION=0.5</code> */
	@Parameter(key = "p_statement_insertion", group = "Search Algorithm", description = "Initial probability of inserting a new statement in a test case")
	public static double P_STATEMENT_INSERTION = 0.5;

	/** Constant <code>P_CHANGE_PARAMETER=0.2</code> */
	@Parameter(key = "p_change_parameter", group = "Search Algorithm", description = "Initial probability of inserting a new statement in a test case")
	public static double P_CHANGE_PARAMETER = 0.2;

	/** Constant <code>P_TEST_DELETE=1d / 3d</code> */
	@Parameter(key = "p_test_delete", group = "Search Algorithm", description = "Probability of deleting statements during mutation")
	public static double P_TEST_DELETE = 1d / 3d;

	/** Constant <code>P_TEST_CHANGE=1d / 3d</code> */
	@Parameter(key = "p_test_change", group = "Search Algorithm", description = "Probability of changing statements during mutation")
	public static double P_TEST_CHANGE = 1d / 3d;

	/** Constant <code>P_TEST_INSERT=1d / 3d</code> */
	@Parameter(key = "p_test_insert", group = "Search Algorithm", description = "Probability of inserting new statements during mutation")
	public static double P_TEST_INSERT = 1d / 3d;

	/** Constant <code>KINCOMPENSATION=1.0</code> */
	@Parameter(key = "kincompensation", group = "Search Algorithm", description = "Penalty for duplicate individuals")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double KINCOMPENSATION = 1.0;

	/** Constant <code>ELITE=1</code> */
	@Parameter(key = "elite", group = "Search Algorithm", description = "Elite size for search algorithm")
	public static int ELITE = 1;

	/** Constant <code>TOURNAMENT_SIZE=10</code> */
	@Parameter(key = "tournament_size", group = "Search Algorithm", description = "Number of individuals for tournament selection")
	public static int TOURNAMENT_SIZE = 10;

	/** Constant <code>RANK_BIAS=1.7</code> */
	@Parameter(key = "rank_bias", group = "Search Algorithm", description = "Bias for better individuals in rank selection")
	public static double RANK_BIAS = 1.7;

	/** Constant <code>CHROMOSOME_LENGTH=40</code> */
	@Parameter(key = "chromosome_length", group = "Search Algorithm", description = "Maximum length of chromosomes during search")
	@IntValue(min = 1, max = 100000)
	public static int CHROMOSOME_LENGTH = 40;

	/** Constant <code>POPULATION=50</code> */
	@Parameter(key = "population", group = "Search Algorithm", description = "Population size of genetic algorithm")
	@IntValue(min = 1)
	public static int POPULATION = 50;

	public enum PopulationLimit {
		INDIVIDUALS, TESTS, STATEMENTS;
	}

	/** Constant <code>POPULATION_LIMIT</code> */
	@Parameter(key = "population_limit", group = "Search Algorithm", description = "What to use as limit for the population size")
	public static PopulationLimit POPULATION_LIMIT = PopulationLimit.INDIVIDUALS;

	/** Constant <code>SEARCH_BUDGET=60</code> */
	@Parameter(key = "search_budget", group = "Search Algorithm", description = "Maximum search duration")
	@LongValue(min = 1)
	public static long SEARCH_BUDGET = 60;

	/** Constant <code>OUTPUT_DIR="evosuite-files"</code> */
	@Parameter(key = "OUTPUT_DIR", group = "Runtime", description = "Directory in which to put generated files")
	public static String OUTPUT_DIR = "evosuite-files";

	/**
	 * Constant
	 * <code>PROPERTIES_FILE="OUTPUT_DIR + File.separatorevosuite.pro"{trunked}</code>
	 */
	public static String PROPERTIES_FILE = OUTPUT_DIR + File.separator
	        + "evosuite.properties";

	public enum StoppingCondition {
		MAXSTATEMENTS, MAXTESTS, MAXTIME, MAXGENERATIONS, MAXFITNESSEVALUATIONS
	}

	/** Constant <code>STOPPING_CONDITION</code> */
	@Parameter(key = "stopping_condition", group = "Search Algorithm", description = "What condition should be checked to end the search")
	public static StoppingCondition STOPPING_CONDITION = StoppingCondition.MAXTIME;

	public enum CrossoverFunction {
		SINGLEPOINTRELATIVE, SINGLEPOINTFIXED, SINGLEPOINT, COVERAGE
	}

	/** Constant <code>CROSSOVER_FUNCTION</code> */
	@Parameter(key = "crossover_function", group = "Search Algorithm", description = "Crossover function during search")
	public static CrossoverFunction CROSSOVER_FUNCTION = CrossoverFunction.SINGLEPOINTRELATIVE;

	public enum TheReplacementFunction {
		/**
		 * Indicates a replacement function which works for all chromosomes
		 * because it solely relies on fitness values.
		 */
		FITNESSREPLACEMENT,
		/**
		 * EvoSuite's default replacement function which only works on subtypes
		 * of the default chromosome types. Relies on fitness plus secondary
		 * goals such as length.
		 */
		DEFAULT
	}

	/**
	 * During search the genetic algorithm has to decide whether the parent
	 * chromosomes or the freshly created offspring chromosomes should be
	 * preferred. If you use EvoSuite with its default chromosomes the
	 * TheReplacementFunction.DEFAULT is what you want. If your chromosomes are
	 * not a subclass of the default chromosomes your have to write your own
	 * replacement function or use TheReplacementFunction.FITNESSREPLACEMENT.
	 */
	@Parameter(key = "replacement_function", group = "Search Algorithm", description = "Replacement function for comparing offspring to parents during search")
	public static TheReplacementFunction REPLACEMENT_FUNCTION = TheReplacementFunction.DEFAULT;

	public enum SelectionFunction {
		RANK, ROULETTEWHEEL, TOURNAMENT
	}

	/** Constant <code>SELECTION_FUNCTION</code> */
	@Parameter(key = "selection_function", group = "Search Algorithm", description = "Selection function during search")
	public static SelectionFunction SELECTION_FUNCTION = SelectionFunction.RANK;

	// TODO: Fix values
	/** Constant <code>SECONDARY_OBJECTIVE="totallength"</code> */
	@Parameter(key = "secondary_objectives", group = "Search Algorithm", description = "Secondary objective during search")
	// @SetValue(values = { "maxlength", "maxsize", "avglength", "none" })
	public static String SECONDARY_OBJECTIVE = "totallength";

	/** Constant <code>BLOAT_FACTOR=2</code> */
	@Parameter(key = "bloat_factor", group = "Search Algorithm", description = "Maximum relative increase in length")
	public static int BLOAT_FACTOR = 2;

	/** Constant <code>STOP_ZERO=true</code> */
	@Parameter(key = "stop_zero", group = "Search Algorithm", description = "Stop optimization once goal is covered")
	public static boolean STOP_ZERO = true;

	/** Constant <code>DYNAMIC_LIMIT=false</code> */
	@Parameter(key = "dynamic_limit", group = "Search Algorithm", description = "Multiply search budget by number of test goals")
	public static boolean DYNAMIC_LIMIT = false;

	/** Constant <code>GLOBAL_TIMEOUT=600</code> */
	@Parameter(key = "global_timeout", group = "Search Algorithm", description = "Maximum seconds allowed for entire search when not using time as stopping criterion")
	@IntValue(min = 0)
	public static int GLOBAL_TIMEOUT = 600;

	/** Constant <code>MINIMIZATION_TIMEOUT=600</code> */
	@Parameter(key = "minimization_timeout", group = "Search Algorithm", description = "Seconds allowed for minimization at the end")
	@IntValue(min = 0)
	public static int MINIMIZATION_TIMEOUT = 600;

	@Parameter(key = "assertion_timeout", group = "Search Algorithm", description = "Seconds allowed for assertion generation at the end")
	@IntValue(min = 0)
	public static int ASSERTION_TIMEOUT = 600;

	/** Constant <code>EXTRA_TIMEOUT=120</code> */
	@Parameter(key = "extra_timeout", group = "Search Algorithm", description = "Extra seconds allowed for the search")
	@IntValue(min = 0)
	public static int EXTRA_TIMEOUT = 120;

	@Parameter(key = "analysis_criteria", group = "Output", description = "List of criteria which should be measured on the completed test suite")
	public static String ANALYSIS_CRITERIA = "";

	//----------------------------------------------------------------
	// Continuous Test Generation

	@Parameter(key = "ctg_memory", group = "Continuous Test Generation", description = "Total Memory (in MB) that CTG will use")
	public static int CTG_MEMORY = 1000;

	@Parameter(key = "ctg_cores", group = "Continuous Test Generation", description = "Number of cores CTG will use")
	public static int CTG_CORES = 1;

	@Parameter(key = "ctg_time", group = "Continuous Test Generation", description = "How many minutes in total CTG will run")
	public static int CTG_TIME = 2;

	/*
	 * FIXME choose best schedule for default
	 * Note: most likely we ll use this parameter only for testing/experiments.
	 * Maven plugin will use the default, best one
	 */
	@Parameter(key = "ctg_schedule", group = "Continuous Test Generation", description = "Schedule used to run jobs")
	public static AvailableSchedule CTG_SCHEDULE = AvailableSchedule.SIMPLE;

	// ---------------------------------------------------------------
	// Single branch mode
	/** Constant <code>RANDOM_TESTS=0</code> */
	@Parameter(key = "random_tests", group = "Single Branch Mode", description = "Number of random tests to run before test generation (Single branch mode)")
	public static int RANDOM_TESTS = 0;

	/** Constant <code>SKIP_COVERED=true</code> */
	@Parameter(key = "skip_covered", group = "Single Branch Mode", description = "Skip coverage goals that have already been (coincidentally) covered")
	public static boolean SKIP_COVERED = true;

	/** Constant <code>REUSE_BUDGET=true</code> */
	@Parameter(key = "reuse_budget", group = "Single Branch Mode", description = "Use leftover budget on unsatisfied test goals (Single branch mode)")
	public static boolean REUSE_BUDGET = true;

	/** Constant <code>SHUFFLE_GOALS=true</code> */
	@Parameter(key = "shuffle_goals", group = "Single Branch Mode", description = "Shuffle test goals before test generation (Single branch mode)")
	public static boolean SHUFFLE_GOALS = true;

	/** Constant <code>RECYCLE_CHROMOSOMES=true</code> */
	@Parameter(key = "recycle_chromosomes", group = "Single Branch Mode", description = "Seed initial population with related individuals (Single branch mode)")
	public static boolean RECYCLE_CHROMOSOMES = true;

	// ---------------------------------------------------------------
	// Output
	public enum OutputFormat {
		JUNIT3, JUNIT4, TESTNG
	}

	/** Constant <code>TEST_FORMAT</code> */
	@Parameter(key = "test_format", group = "Output", description = "Format of the resulting test cases")
	public static OutputFormat TEST_FORMAT = OutputFormat.JUNIT4;

	@Parameter(key = "structured_tests", group = "Output", description = "Structure tests according to setup - exercise - check")
	public static boolean STRUCTURED_TESTS = false;

	@Parameter(key = "test_comments", group = "Output", description = "Include a header with coverage information for each test")
	public static boolean TEST_COMMENTS = true;

	/** Constant <code>PRINT_TO_SYSTEM=false</code> */
	@Parameter(key = "print_to_system", group = "Output", description = "Allow test output on console")
	public static boolean PRINT_TO_SYSTEM = false;

	/** Constant <code>PLOT=false</code> */
	@Parameter(key = "plot", group = "Output", description = "Create plots of size and fitness")
	public static boolean PLOT = false;

	/** Constant <code>HTML=true</code> */
	@Parameter(key = "html", group = "Output", description = "Create html reports")
	public static boolean HTML = true;

	/** Constant <code>JUNIT_TESTS=true</code> */
	@Parameter(key = "junit_tests", group = "Output", description = "Create JUnit test suites")
	public static boolean JUNIT_TESTS = true;

	/**
	 * TODO: this functionality is not implemented yet
	 */
	@Parameter(key = "junit_extend", group = "Output", description = "Extend existing JUnit test suite")
	public static String JUNIT_EXTEND = "";

	/** Constant <code>JUNIT_PREFIX=""</code> */
	@Parameter(key = "junit_prefix", group = "Experimental", description = "Prefix of JUnit tests to execute")
	public static String JUNIT_PREFIX = "";

	/** Constant <code>LOG_GOALS=false</code> */
	@Parameter(key = "log_goals", group = "Output", description = "Create a CSV file for each individual evolution")
	public static boolean LOG_GOALS = false;

	@Parameter(key = "log.level", group = "Output", description = "Verbosity level of logger")
	public static String LOG_LEVEL = null;

	@Parameter(key = "log.target", group = "Output", description = "Target logger - all logging if not set")
	public static String LOG_TARGET = null;

	/** Constant <code>MINIMIZE=true</code> */
	@Parameter(key = "minimize", group = "Output", description = "Minimize test suite after generation")
	public static boolean MINIMIZE = true;

	/** Constant <code>COVERAGE=true</code> */
	@Parameter(key = "coverage", group = "Output", description = "Minimize test suite after generation")
	public static boolean COVERAGE = true;

	/** Constant <code>MINIMIZE_OLD=false</code> */
	@Parameter(key = "minimize_old", group = "Output", description = "Minimize test suite using old algorithm")
	public static boolean MINIMIZE_OLD = false;

	/** Constant <code>INLINE=false</code> */
	@Parameter(key = "inline", group = "Output", description = "Inline all constants")
	public static boolean INLINE = false;

	/** Constant <code>MINIMIZE_VALUES=false</code> */
	@Parameter(key = "minimize_values", group = "Output", description = "Minimize constants and method calls")
	public static boolean MINIMIZE_VALUES = false;

	/** Constant <code>WRITE_POOL=false</code> */
	@Parameter(key = "write_pool", group = "Output", description = "Keep sequences for object pool")
	public static boolean WRITE_POOL = false;

	/** Constant <code>REPORT_DIR="evosuite-report"</code> */
	@Parameter(key = "report_dir", group = "Output", description = "Directory in which to put HTML and CSV reports")
	public static String REPORT_DIR = "evosuite-report";

	/** Constant <code>OUTPUT_VARIABLES=null</code> */
	@Parameter(key = "output_variables", group = "Output", description = "List of variables to output to CSV file. Variables are separated by commas. Null represents default values")
	public static String OUTPUT_VARIABLES = null;

	/** Constant <code>CONFIGURATION_ID=null</code> */
	@Parameter(key = "configuration_id", group = "Output", description = "Label used to identify the used configuration of EvoSuite. This is only done when running experiments.")
	public static String CONFIGURATION_ID = null;

	/** Constant <code>SAVE_ALL_DATA=true</code> */
	@Parameter(key = "save_all_data", group = "Output", description = "Generate and store all data reports")
	public static boolean SAVE_ALL_DATA = true;

	/** Constant <code>PRINT_CURRENT_GOALS=false</code> */
	@Parameter(key = "print_current_goals", group = "Output", description = "Print out current goal during test generation")
	public static boolean PRINT_CURRENT_GOALS = false;

	/** Constant <code>PRINT_COVERED_GOALS=false</code> */
	@Parameter(key = "print_covered_goals", group = "Output", description = "Print out covered goals during test generation")
	public static boolean PRINT_COVERED_GOALS = false;

	/** Constant <code>ASSERTIONS=false</code> */
	@Parameter(key = "assertions", group = "Output", description = "Create assertions")
	public static boolean ASSERTIONS = true;

	public enum AssertionStrategy {
		ALL, MUTATION, UNIT
	}

	/** Constant <code>ASSERTION_STRATEGY</code> */
	@Parameter(key = "assertion_strategy", group = "Output", description = "Which assertions to generate")
	public static AssertionStrategy ASSERTION_STRATEGY = AssertionStrategy.MUTATION;

	@Parameter(key = "filter_assertions", group = "Output", description = "Filter flaky assertions")
	public static boolean FILTER_ASSERTIONS = true;

	@Parameter(key = "max_mutants_per_test", group = "Output", description = "How many mutants to use when trying to find assertions for a test")
	public static int MAX_MUTANTS_PER_TEST = 100;

	@Parameter(key = "max_mutants_per_method", group = "Output", description = "How many mutants can be inserted into a single method")
	public static int MAX_MUTANTS_PER_METHOD = 700;

	@Parameter(key = "max_replace_mutants", group = "Output", description = "How many replacement mutants can be inserted for any one variable")
	public static int MAX_REPLACE_MUTANTS = 100;

	/** Constant <code>TEST_DIR="evosuite-tests"</code> */
	@Parameter(key = "test_dir", group = "Output", description = "Directory in which to place JUnit tests")
	public static String TEST_DIR = "evosuite-tests";

	/** Constant <code>WRITE_CFG=false</code> */
	@Parameter(key = "write_cfg", group = "Output", description = "Create CFG graphs")
	public static boolean WRITE_CFG = false;

	/** Constant <code>SHUTDOWN_HOOK=true</code> */
	@Parameter(key = "shutdown_hook", group = "Output", description = "Store test suite on Ctrl+C")
	public static boolean SHUTDOWN_HOOK = true;

	/** Constant <code>SHOW_PROGRESS=true</code> */
	@Parameter(key = "show_progress", group = "Output", description = "Show progress bar on console")
	public static boolean SHOW_PROGRESS = true;

	/** Constant <code>SERIALIZE_RESULT=false</code> */
	@Parameter(key = "serialize_result", group = "Output", description = "Serialize result of search to main process")
	public static boolean SERIALIZE_RESULT = false;

	@Parameter(key = "new_statistics", group = "Output", description = "Use the new statistics backend on the master")
	public static boolean NEW_STATISTICS = false;

	@Parameter(key = "old_statistics", group = "Output", description = "Use the old statistics backend on the master")
	public static boolean OLD_STATISTICS = true;

	public enum StatisticsBackend {
		NONE, CONSOLE, CSV;
	}

	@Parameter(key = "statistics_backend", group = "Output", description = "Which backend to use to collect data")
	public static StatisticsBackend STATISTICS_BACKEND = StatisticsBackend.CSV;

	/** Constant <code>TIMELINE_INTERVAL=60000</code> */
	@Parameter(key = "timeline_interval", group = "Output", description = "Time interval in milliseconds for timeline statistics")
	public static long TIMELINE_INTERVAL = 60 * 1000;

	public enum OutputGranularity {
		MERGED, TESTCASE
	}

	/** Constant <code>OUTPUT_GRANULARITY</code> */
	@Parameter(key = "output_granularity", group = "Output", description = "Write all test cases for a class into a single file or to separate files.")
	public static OutputGranularity OUTPUT_GRANULARITY = OutputGranularity.MERGED;

	/** Constant <code>MAX_COVERAGE_DEPTH=-1</code> */
	@Parameter(key = "max_coverage_depth", group = "Output", description = "Maximum depth in the calltree to count a branch as covered")
	public static int MAX_COVERAGE_DEPTH = -1;

	// ---------------------------------------------------------------
	// Sandbox
	/** Constant <code>SANDBOX=false</code> */
	@Parameter(key = "sandbox", group = "Sandbox", description = "Execute tests in a sandbox environment")
	public static boolean SANDBOX = true;

	public enum SandboxMode {
		OFF, RECOMMENDED, IO
	}

	/** Constant <code>SANDBOX=false</code> */
	@Parameter(key = "sandbox_mode", group = "Sandbox", description = "Mode in which the sandbox is applied")
	public static SandboxMode SANDBOX_MODE = SandboxMode.RECOMMENDED;

	@Parameter(key = "filter_sandbox_tests", group = "Sandbox", description = "Drop tests that require the sandbox")
	public static boolean FILTER_SANDBOX_TESTS = false;

	/** Constant <code>VIRTUAL_FS=false</code> */
	@Parameter(key = "virtual_fs", group = "Sandbox", description = "Usage of ram fs")
	public static boolean VIRTUAL_FS = false;

	/** Constant <code>READ_ONLY_FROM_SANDBOX_FOLDER = false;</code> */
	@Parameter(key = "restricted_read", group = "Sandbox", description = "Determines if the VFS shall only be allowed to read files from the sandbox read folder")
	public static boolean READ_ONLY_FROM_SANDBOX_FOLDER = false;

	/** Constant <code>SANDBOX_FOLDER="evosuite-sandbox"</code> */
	@Parameter(key = "sandbox_folder", group = "Sandbox", description = "Folder used for IO, when mocks are enabled")
	public static String SANDBOX_FOLDER = "evosuite-sandbox";

	// ---------------------------------------------------------------
	// Experimental

	@Parameter(key = "cluster_recursion", description = "The maximum level of recursion when calculating the dependencies in the test cluster")
	public static int CLUSTER_RECURSION = 10;

	@Parameter(key = "inheritance_file", description = "Cached version of inheritance tree")
	public static String INHERITANCE_FILE = "";

	/** Constant <code>BRANCH_EVAL=false</code> */
	@Parameter(key = "branch_eval", description = "Jeremy's branch evaluation")
	public static boolean BRANCH_EVAL = false;

	/** Constant <code>BRANCH_STATEMENT=false</code> */
	@Parameter(key = "branch_statement", description = "Require statement coverage for branch coverage")
	public static boolean BRANCH_STATEMENT = false;

	/** Constant <code>REMOTE_TESTING=false</code> */
	@Parameter(key = "remote_testing", description = "Include remote calls")
	public static boolean REMOTE_TESTING = false;

	/** Constant <code>CPU_TIMEOUT=false</code> */
	@Parameter(key = "cpu_timeout", description = "Measure timeouts on CPU time, not global time")
	public static boolean CPU_TIMEOUT = false;

	/** Constant <code>LOG_TIMEOUT=false</code> */
	@Parameter(key = "log_timeout", description = "Produce output each time a test times out")
	public static boolean LOG_TIMEOUT = false;

	/** Constant <code>CALL_PROBABILITY=0.0</code> */
	@Parameter(key = "call_probability", description = "Probability to reuse an existing test case, if it produces a required object")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CALL_PROBABILITY = 0.0;

	/** Constant <code>USAGE_MODELS=""</code> */
	@Parameter(key = "usage_models", description = "Names of usage model files")
	public static String USAGE_MODELS = "";

	/** Constant <code>USAGE_RATE=0.5</code> */
	@Parameter(key = "usage_rate", description = "Probability with which to use transitions out of the OUM")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double USAGE_RATE = 0.5;

	/** Constant <code>INSTRUMENTATION_SKIP_DEBUG=false</code> */
	@Parameter(key = "instrumentation_skip_debug", description = "Skip debug information in bytecode instrumentation (needed for compatibility with classes transformed by Emma code instrumentation due to an ASM bug)")
	public static boolean INSTRUMENTATION_SKIP_DEBUG = false;

	/** Constant <code>INSTRUMENT_PARENT=false</code> */
	@Parameter(key = "instrument_parent", description = "Also count coverage goals in superclasses")
	public static boolean INSTRUMENT_PARENT = false;

	@Parameter(key = "instrument_context", description = "Also instrument methods called from the SUT")
	public static boolean INSTRUMENT_CONTEXT = false;

	/** Constant <code>BREAK_ON_EXCEPTION=true</code> */
	@Parameter(key = "break_on_exception", description = "Stop test execution if exception occurrs")
	public static boolean BREAK_ON_EXCEPTION = true;

	public enum TestFactory {
		RANDOM, ALLMETHODS, TOURNAMENT, JUNIT
	}

	/** Constant <code>TEST_FACTORY</code> */
	@Parameter(key = "test_factory", description = "Which factory creates tests")
	public static TestFactory TEST_FACTORY = TestFactory.RANDOM;

	@Parameter(key = "selected_junit", description = "List of fully qualified class names (separated by ':') indicating which JUnit test suites the user has selected (e.g., for seeding)")
	public static String SELECTED_JUNIT = null;

	/** Constant <code>JUNIT_STRICT=false</code> */
	@Parameter(key = "junit_strict", description = "Only include test files containing the target classname")
	public static boolean JUNIT_STRICT = false;

	/** Constant <code>SEED_CLONE=0.2</code> */
	@Parameter(key = "seed_clone", description = "Probability with which existing individuals are cloned")
	public static double SEED_CLONE = 0.2;

	/** Constant <code>SEED_MUTATIONS=2</code> */
	@Parameter(key = "seed_mutations", description = "Probability with which cloned individuals are mutated")
	public static int SEED_MUTATIONS = 2;

	/** Constant <code>CONCOLIC_MUTATION=0.0</code> */
	@Parameter(key = "concolic_mutation", description = "Probability of using concolic mutation operator")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double CONCOLIC_MUTATION = 0.0;

	@Parameter(key = "constraint_solution_attempts", description = "Number of attempts to solve constraints related to one code branch")
	public static int CONSTRAINT_SOLUTION_ATTEMPTS = 3;

	/** Constant <code>TESTABILITY_TRANSFORMATION=false</code> */
	@Parameter(key = "testability_transformation", description = "Apply testability transformation (Yanchuan)")
	public static boolean TESTABILITY_TRANSFORMATION = false;

	/** Constant <code>TT_stack=10</code> */
	@Parameter(key = "TT_stack", description = "Maximum stack depth for testability transformation")
	public static int TT_stack = 10;

	/** Constant <code>TT=false</code> */
	@Parameter(key = "TT", description = "Testability transformation")
	public static boolean TT = false;

	public enum TransformationScope {
		TARGET, PREFIX, ALL
	}

	/** Constant <code>TT_SCOPE</code> */
	@Parameter(key = "tt_scope", description = "Testability transformation")
	public static TransformationScope TT_SCOPE = TransformationScope.ALL;

	// ---------------------------------------------------------------
	// Contracts / Asserts:
	/** Constant <code>CHECK_CONTRACTS=false</code> */
	@Parameter(key = "check_contracts", description = "Check contracts during test execution")
	public static boolean CHECK_CONTRACTS = false;

	/** Constant <code>CHECK_CONTRACTS_END=false</code> */
	@Parameter(key = "check_contracts_end", description = "Check contracts only once per test")
	public static boolean CHECK_CONTRACTS_END = false;

	/** Constant <code>ERROR_BRANCHES=false</code> */
	@Parameter(key = "error_branches", description = "Instrument code with error checking branches")
	public static boolean ERROR_BRANCHES = false;

	/*
	 * FIXME: these 2 following properties will not work if we use the EvoSuite shell script which call MasterProcess directly rather than
	 * EvoSuite.java
	 */

	/** Constant <code>ENABLE_ASSERTS_FOR_EVOSUITE=false</code> */
	@Parameter(key = "enable_asserts_for_evosuite", description = "When running EvoSuite clients, for debugging purposes check its assserts")
	public static boolean ENABLE_ASSERTS_FOR_EVOSUITE = false;

	/** Constant <code>ENABLE_ASSERTS_FOR_SUT=true</code> */
	@Parameter(key = "enable_asserts_for_sut", description = "Check asserts in the SUT")
	public static boolean ENABLE_ASSERTS_FOR_SUT = true;

	// ---------------------------------------------------------------
	// Test Execution
	/** Constant <code>TIMEOUT=5000</code> */
	@Parameter(key = "timeout", group = "Test Execution", description = "Milliseconds allowed per test")
	public static int TIMEOUT = 5000;

	@Parameter(key = "concolic_timeout", group = "Test Execution", description = "Milliseconds allowed per test during concolic execution")
	public static int CONCOLIC_TIMEOUT = 15000;

	/** Constant <code>SHUTDOWN_TIMEOUT=1000</code> */
	@Parameter(key = "shutdown_timeout", group = "Test Execution", description = "Milliseconds grace time to shut down test cleanly")
	public static int SHUTDOWN_TIMEOUT = 1000;

	/** Constant <code>MUTATION_TIMEOUTS=3</code> */
	@Parameter(key = "mutation_timeouts", group = "Test Execution", description = "Number of timeouts before we consider a mutant killed")
	public static int MUTATION_TIMEOUTS = 3;

	/** Constant <code>ARRAY_LIMIT=1000000</code> */
	@Parameter(key = "array_limit", group = "Test Execution", description = "Hard limit on array allocation in the code")
	public static int ARRAY_LIMIT = 1000000;

	/** Constant <code>MAX_MUTANTS=100</code> */
	@Parameter(key = "max_mutants", group = "Test Execution", description = "Maximum number of mutants to target at the same time")
	public static int MAX_MUTANTS = 100;

	/** Constant <code>MUTATION_GENERATIONS=10</code> */
	@Parameter(key = "mutation_generations", group = "Test Execution", description = "Number of generations before changing the currently targeted mutants")
	public static int MUTATION_GENERATIONS = 10;

	/** Constant <code>REPLACE_CALLS=false</code> */
	@Parameter(key = "replace_calls", group = "Test Execution", description = "Replace nondeterministic calls and System.exit")
	public static boolean REPLACE_CALLS = false;

	// ---------------------------------------------------------------
	// Debugging

	/** Constant <code>DEBUG=false</code> */
	@Parameter(key = "debug", group = "Debugging", description = "Enables debugging support in the client VM")
	public static boolean DEBUG = false;

	/** Constant <code>PORT=1044</code> */
	@Parameter(key = "port", group = "Debugging", description = "Port on localhost, to which the client VM will listen for a remote debugger; defaults to 1044")
	@IntValue(min = 1024, max = 65535)
	public static int PORT = 1044;

	// ---------------------------------------------------------------
	// TODO: Fix description
	public enum AlternativeFitnessCalculationMode {
		SUM, MIN, MAX, AVG, SINGLE
	}

	/** Constant <code>ALTERNATIVE_FITNESS_CALCULATION_MODE</code> */
	@Parameter(key = "alternative_fitness_calculation_mode", description = "")
	public static AlternativeFitnessCalculationMode ALTERNATIVE_FITNESS_CALCULATION_MODE = AlternativeFitnessCalculationMode.SUM;

	/** Constant <code>INITIALLY_ENFORCED_RANDOMNESS=0.4</code> */
	@Parameter(key = "initially_enforced_randomness", description = "")
	@DoubleValue(min = 0.0, max = 1.0)
	public static double INITIALLY_ENFORCED_RANDOMNESS = 0.4;

	/** Constant <code>ALTERNATIVE_FITNESS_RANGE=100.0</code> */
	@Parameter(key = "alternative_fitness_range", description = "")
	public static double ALTERNATIVE_FITNESS_RANGE = 100.0;

	/** Constant <code>STARVE_BY_FITNESS=true</code> */
	@Parameter(key = "starve_by_fitness", description = "")
	public static boolean STARVE_BY_FITNESS = true;

	/** Constant <code>PENALIZE_OVERWRITING_DEFINITIONS_FLAT=false</code> */
	@Parameter(key = "penalize_overwriting_definitions_flat", description = "")
	public static boolean PENALIZE_OVERWRITING_DEFINITIONS_FLAT = false;

	/** Constant <code>PENALIZE_OVERWRITING_DEFINITIONS_LINEARLY=false</code> */
	@Parameter(key = "penalize_overwriting_definitions_linearly", description = "")
	public static boolean PENALIZE_OVERWRITING_DEFINITIONS_LINEARLY = false;

	/** Constant <code>ENABLE_ALTERNATIVE_FITNESS_CALCULATION=false</code> */
	@Parameter(key = "enable_alternative_fitness_calculation", description = "")
	public static boolean ENABLE_ALTERNATIVE_FITNESS_CALCULATION = false;

	/** Constant <code>ENABLE_ALTERNATIVE_FITNESS_CALCULATION=false</code> */
	@Parameter(key = "enable_alternative_suite_fitness", description = "")
	public static boolean ENABLE_ALTERNATIVE_SUITE_FITNESS = false;

	/** Constant <code>DEFUSE_DEBUG_MODE=false</code> */
	@Parameter(key = "defuse_debug_mode", description = "")
	public static boolean DEFUSE_DEBUG_MODE = false;

	/** Constant <code>RANDOMIZE_DIFFICULTY=true</code> */
	@Parameter(key = "randomize_difficulty", description = "")
	public static boolean RANDOMIZE_DIFFICULTY = true;

	// ---------------------------------------------------------------
	// UI Test generation parameters
	/** Constant <code>UI_BACKGROUND_COVERAGE_DELAY=-1</code> */
	@Parameter(key = "UI_BACKGROUND_COVERAGE_DELAY", group = "EXSYST", description = "How often to write out coverage information in the background (in ms). -1 to disable.")
	public static int UI_BACKGROUND_COVERAGE_DELAY = -1;

	// ---------------------------------------------------------------
	// Runtime parameters

	public enum Criterion {
		EXCEPTION,
		LCSAJ,
		DEFUSE,
		ALLDEFS,
		PATH,
		BRANCH,
		STRONGMUTATION,
		WEAKMUTATION,
		MUTATION,
		COMP_LCSAJ_BRANCH,
		STATEMENT,
		DATA,
		BEHAVIORAL,
		IBRANCH,
		REGRESSION
	}

	/** Cache target class */
	private static Class<?> TARGET_CLASS_INSTANCE = null;

	/** Constant <code>CP=""</code> */
	@Parameter(key = "CP", group = "Runtime", description = "The classpath of the target classes")
	public static String CP = "";

	/** Constant <code>PROJECT_PREFIX="null"</code> */
	@Parameter(key = "PROJECT_PREFIX", group = "Runtime", description = "Package name of target package")
	public static String PROJECT_PREFIX = "";

	/** Constant <code>PROJECT_DIR="null"</code> */
	@Parameter(key = "PROJECT_DIR", group = "Runtime", description = "Directory name of target package")
	public static String PROJECT_DIR = null;

	/** Package name of target class (might be a subpackage) */
	public static String CLASS_PREFIX = "";

	/** Sub-package name of target class */
	public static String SUB_PREFIX = "";

	/** Constant <code>TARGET_CLASS_PREFIX=""</code> */
	@Parameter(key = "TARGET_CLASS_PREFIX", group = "Runtime", description = "Prefix of classes we are trying to cover")
	public static String TARGET_CLASS_PREFIX = "";

	/** Class under test */
	@Parameter(key = "TARGET_CLASS", group = "Runtime", description = "Class under test")
	public static String TARGET_CLASS = "";

	/** Method under test */
	@Parameter(key = "target_method", group = "Runtime", description = "Method for which to generate tests")
	public static String TARGET_METHOD = "";

	/** Method under test */
	@Parameter(key = "target_method_prefix", group = "Runtime", description = "All methods matching prefix will be used for generating tests")
	public static String TARGET_METHOD_PREFIX = "";
	
	/** Method under test */
	@Parameter(key = "target_method_list", group = "Runtime", description = "A semi-colon separated list of methods for which to generate tests")
	public static String TARGET_METHOD_LIST= "";
	
	/** Constant <code>HIERARCHY_DATA="hierarchy.xml"</code> */
	@Parameter(key = "hierarchy_data", group = "Runtime", description = "File in which hierarchy data is stored")
	public static String HIERARCHY_DATA = "hierarchy.xml";

	/** Constant <code>CONNECTION_DATA="connection.xml"</code> */
	@Parameter(key = "connection_data", group = "Runtime", description = "File in which connection data is stored")
	public static String CONNECTION_DATA = "connection.xml";

	/** Constant <code>CRITERION</code> */
	@Parameter(key = "criterion", group = "Runtime", description = "Coverage criterion")
	public static Criterion CRITERION = Criterion.BRANCH;

	public enum Strategy {
		ONEBRANCH, EVOSUITE, RANDOM, RANDOM_FIXED, REGRESSION
	}

	/** Constant <code>STRATEGY</code> */
	@Parameter(key = "strategy", group = "Runtime", description = "Which mode to use")
	public static Strategy STRATEGY = Strategy.EVOSUITE;

	/** Constant <code>PROCESS_COMMUNICATION_PORT=-1</code> */
	@Parameter(key = "process_communication_port", group = "Runtime", description = "Port at which the communication with the external process is done")
	public static int PROCESS_COMMUNICATION_PORT = -1;

	/** Constant <code>STOPPING_PORT=-1</code> */
	@Parameter(key = "stopping_port", group = "Runtime", description = "Port at which a stopping condition waits for interruption")
	public static int STOPPING_PORT = -1;

	/** Constant <code>MAX_STALLED_THREADS=10</code> */
	@Parameter(key = "max_stalled_threads", group = "Runtime", description = "Number of stalled threads")
	public static int MAX_STALLED_THREADS = 10;

	/** Constant <code>MIN_FREE_MEM=50 * 1000 * 1000</code> */
	@Parameter(key = "min_free_mem", group = "Runtime", description = "Minimum amount of available memory")
	public static int MIN_FREE_MEM = 50 * 1000 * 1000;

	/** Constant <code>CLIENT_ON_THREAD=false</code> */
	@Parameter(key = "client_on_thread", group = "Runtime", description = "Run client process on same JVM of master in separate thread. To be used only for debugging purposes")
	public static volatile boolean CLIENT_ON_THREAD = false;

	// ---------------------------------------------------------------
	// Seeding test cases

	/** Constant <code>CLASSPATH="new String[] {  }"</code> */
	@Parameter(key = "classpath", group = "Test Seeding", description = "The classpath needed to compile the seeding test case.")
	public static String[] CLASSPATH = new String[] { "" };

	/** Constant <code>SOURCEPATH="new String[] {  }"</code> */
	@Parameter(key = "sourcepath", group = "Test Seeding", description = "The path to the test case source.")
	public static String[] SOURCEPATH = new String[] { "" };

	/**
	 * Get all parameters that are available
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<String> getParameters() {
		return parameterMap.keySet();
	}

	/**
	 * Determine fields that are declared as parameters
	 */
	private static void reflectMap() {
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);
				parameterMap.put(p.key(), f);
				try {
					defaultMap.put(f, f.get(null));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
	}

	/**
	 * Initialize properties from property file or command line parameters
	 */
	private void initializeProperties() {
		for (String parameter : parameterMap.keySet()) {
			try {
				String property = System.getProperty(parameter);
				if (property == null) {
					property = properties.getProperty(parameter);
				}
				if (property != null) {
					setValue(parameter, property);
					// System.out.println("Loading property " + parameter + "="
					// + property);
				}
			} catch (NoSuchParameterException e) {
				logger.info("- No such parameter: " + parameter);
			} catch (IllegalArgumentException e) {
				logger.info("- Error setting parameter \"" + parameter + "\": " + e);
			} catch (IllegalAccessException e) {
				logger.info("- Error setting parameter \"" + parameter + "\": " + e);
			}
		}
		if (POPULATION_LIMIT == PopulationLimit.STATEMENTS) {
			if (MAX_LENGTH < POPULATION) {
				MAX_LENGTH = POPULATION;
			}
		}
	}

	/**
	 * Load and initialize a properties file from the default path
	 */
	public void loadProperties(boolean silent) {
		loadPropertiesFile(System.getProperty(PROPERTIES_FILE,
		                                      "evosuite-files/evosuite.properties"),
		                   silent);
		initializeProperties();
	}

	/**
	 * Load and initialize a properties file from a given path
	 * 
	 * @param propertiesPath
	 *            a {@link java.lang.String} object.
	 */
	public void loadProperties(String propertiesPath, boolean silent) {
		loadPropertiesFile(propertiesPath, silent);
		initializeProperties();
	}

	/**
	 * Load a properties file
	 * 
	 * @param propertiesPath
	 *            a {@link java.lang.String} object.
	 */
	public void loadPropertiesFile(String propertiesPath, boolean silent) {
		properties = new java.util.Properties();
		try {
			InputStream in = null;
			File propertiesFile = new File(propertiesPath);
			if (propertiesFile.exists()) {
				in = new FileInputStream(propertiesPath);
				properties.load(in);

				if (!silent)
					LoggingUtils.getEvoLogger().info("* Properties loaded from "
					                                         + propertiesFile.getAbsolutePath());
			} else {
				propertiesPath = "evosuite.properties";
				in = this.getClass().getClassLoader().getResourceAsStream(propertiesPath);
				if (in != null) {
					properties.load(in);
					if (!silent)
						LoggingUtils.getEvoLogger().info("* Properties loaded from "
						                                         + this.getClass().getClassLoader().getResource(propertiesPath).getPath());
				}
				// logger.info("* Properties loaded from default configuration file.");
			}
		} catch (FileNotFoundException e) {
			logger.info("- Error: Could not find configuration file " + propertiesPath);
		} catch (IOException e) {
			logger.info("- Error: Could not find configuration file " + propertiesPath);
		} catch (Exception e) {
			logger.info("- Error: Could not find configuration file " + propertiesPath);
		}
	}

	/** All fields representing values, inserted via reflection */
	private static Map<String, Field> parameterMap = new HashMap<String, Field>();

	/** All fields representing values, inserted via reflection */
	private static Map<Field, Object> defaultMap = new HashMap<Field, Object>();

	/**
	 * Keep track of which fields have been changed from their defaults during
	 * loading
	 */
	private static Set<String> changedFields = new HashSet<String>();

	/**
	 * Get class of parameter
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getType(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getType();
	}

	/**
	 * Get description string of parameter
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getDescription(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		Parameter p = f.getAnnotation(Parameter.class);
		return p.description();
	}

	/**
	 * Get group name of parameter
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getGroup(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		Parameter p = f.getAnnotation(Parameter.class);
		return p.group();
	}

	/**
	 * Get integer boundaries
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.IntValue} object.
	 */
	public static IntValue getIntLimits(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(IntValue.class);
	}

	/**
	 * Get long boundaries
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.LongValue} object.
	 */
	public static LongValue getLongLimits(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(LongValue.class);
	}

	/**
	 * Get double boundaries
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @return a {@link org.evosuite.Properties.DoubleValue} object.
	 */
	public static DoubleValue getDoubleLimits(String key) throws NoSuchParameterException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		return f.getAnnotation(DoubleValue.class);
	}

	/**
	 * Get an integer parameter value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a int.
	 */
	public static int getIntegerValue(String key) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getInt(null);
	}

	/**
	 * Get an integer parameter value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a long.
	 */
	public static long getLongValue(String key) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getLong(null);
	}

	/**
	 * Get a boolean parameter value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a boolean.
	 */
	public static boolean getBooleanValue(String key) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getBoolean(null);
	}

	/**
	 * Get a double parameter value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a double.
	 */
	public static double getDoubleValue(String key) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		return parameterMap.get(key).getDouble(null);
	}

	/**
	 * Get parameter value as string (works for all types)
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getStringValue(String key) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		StringBuffer sb = new StringBuffer();
		Object val = parameterMap.get(key).get(null);
		if (val != null && val.getClass().isArray()) {
			int len = Array.getLength(val);
			for (int i = 0; i < len; i++) {
				if (i > 0)
					sb.append(";");

				sb.append(Array.get(val, i));
			}
		} else {
			sb.append(val);
		}
		return sb.toString();
	}

	/**
	 * Check if there exist any parameter with given name
	 * 
	 * @param parameterName
	 * @return
	 */
	public static boolean hasParameter(String parameterName) {
		return parameterMap.containsKey(parameterName);
	}

	/**
	 * Set parameter to new integer value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a int.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, int value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);

		if (f.isAnnotationPresent(IntValue.class)) {
			IntValue i = f.getAnnotation(IntValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}

		f.setInt(this, value);
	}

	/**
	 * Set parameter to new long value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a long.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, long value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);

		if (f.isAnnotationPresent(LongValue.class)) {
			LongValue i = f.getAnnotation(LongValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}

		f.setLong(this, value);
	}

	/**
	 * Set parameter to new boolean value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a boolean.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 */
	public void setValue(String key, boolean value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		f.setBoolean(this, value);
	}

	/**
	 * Set parameter to new double value
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a double.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	public void setValue(String key, double value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key))
			throw new NoSuchParameterException(key);

		Field f = parameterMap.get(key);
		if (f.isAnnotationPresent(DoubleValue.class)) {
			DoubleValue i = f.getAnnotation(DoubleValue.class);
			if (value < i.min() || value > i.max())
				throw new IllegalArgumentException();
		}
		f.setDouble(this, value);
	}

	/**
	 * Set parameter to new value from String
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            a {@link java.lang.String} object.
	 * @throws org.evosuite.Properties.NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(String key, String value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new NoSuchParameterException(key);
		}

		Field f = parameterMap.get(key);
		changedFields.add(key);
		if (f.getType().isEnum()) {
			f.set(null, Enum.valueOf((Class<Enum>) f.getType(), value.toUpperCase()));
		} else if (f.getType().equals(int.class)) {
			setValue(key, Integer.parseInt(value));
		} else if (f.getType().equals(long.class)) {
			setValue(key, Long.parseLong(value));
		} else if (f.getType().equals(boolean.class)) {
			setValue(key, Boolean.parseBoolean(value));
		} else if (f.getType().equals(double.class)) {
			setValue(key, Double.parseDouble(value));
		} else if (f.getType().isArray()) {
			if (f.getType().isAssignableFrom(String[].class)) {
				setValue(key, value.split(":"));
			}
		} else {
			f.set(null, value);
		}
	}

	/**
	 * <p>
	 * setValue
	 * </p>
	 * 
	 * @param key
	 *            a {@link java.lang.String} object.
	 * @param value
	 *            an array of {@link java.lang.String} objects.
	 * @throws org.evosuite.Properties$NoSuchParameterException
	 *             if any.
	 * @throws java.lang.IllegalArgumentException
	 *             if any.
	 * @throws java.lang.IllegalAccessException
	 *             if any.
	 */
	public void setValue(String key, String[] value) throws NoSuchParameterException,
	        IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new NoSuchParameterException(key);
		}

		Field f = parameterMap.get(key);

		f.set(this, value);
	}

	/** Singleton instance */
	private static Properties instance = null; // new Properties(true, true);

	/** Internal properties hashmap */
	private java.util.Properties properties;

	/**
	 * Singleton accessor
	 * 
	 * @return a {@link org.evosuite.Properties} object.
	 */
	public static Properties getInstance() {
		if (instance == null)
			instance = new Properties(true, false);
		return instance;
	}

	/**
	 * Singleton accessor
	 * 
	 * @return a {@link org.evosuite.Properties} object.
	 */
	public static Properties getInstanceSilent() {
		if (instance == null)
			instance = new Properties(true, true);
		return instance;
	}

	/**
	 * This exception is used when a non-existent parameter is accessed
	 * 
	 * 
	 */
	public static class NoSuchParameterException extends Exception {

		private static final long serialVersionUID = 9074828392047742535L;

		public NoSuchParameterException(String key) {
			super("No such property defined: " + key);
		}
	}

	/** Constructor */
	private Properties(boolean loadProperties, boolean silent) {
		reflectMap();
		if (loadProperties)
			loadProperties(silent);
		setClassPrefix();
	}

	private static void setClassPrefix() {
		if (TARGET_CLASS != null && !TARGET_CLASS.equals("")) {
			if (TARGET_CLASS.contains(".")) {
				CLASS_PREFIX = TARGET_CLASS.substring(0, TARGET_CLASS.lastIndexOf('.'));
				SUB_PREFIX = CLASS_PREFIX.replace(PROJECT_PREFIX + ".", "");
			}
			if (PROJECT_PREFIX == null || PROJECT_PREFIX.equals("")) {
				if (CLASS_PREFIX.contains("."))
					PROJECT_PREFIX = CLASS_PREFIX.substring(0, CLASS_PREFIX.indexOf("."));
				else
					PROJECT_PREFIX = CLASS_PREFIX;
				// LoggingUtils.getEvoLogger().info("* Using project prefix: "
				// + PROJECT_PREFIX);
			}
		}
	}

	/**
	 * Get class object of class under test
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
	public static Class<?> getTargetClass() {
		if (TARGET_CLASS_INSTANCE != null
		        && TARGET_CLASS_INSTANCE.getCanonicalName().equals(TARGET_CLASS))
			return TARGET_CLASS_INSTANCE;

		TARGET_CLASS_INSTANCE = null;

		try {
			TARGET_CLASS_INSTANCE = Class.forName(TARGET_CLASS, true,
			                                      TestGenerationContext.getClassLoader());
			setClassPrefix();

		} catch (ClassNotFoundException e) {
			LoggingUtils.getEvoLogger().info("* Could not find class under test: "
			                                         + Properties.TARGET_CLASS + ": " + e);
			for (StackTraceElement s : e.getStackTrace()) {
				LoggingUtils.getEvoLogger().info("   " + s.toString());
			}
			Throwable cause = e.getCause();
			while (cause != null) {
				LoggingUtils.getEvoLogger().info("Caused by: " + cause);
				for (StackTraceElement s : cause.getStackTrace()) {
					LoggingUtils.getEvoLogger().info("   " + s.toString());
				}
				cause = cause.getCause();
			}
			/*
			 * FIXME: Why this sleep???
			 */
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				logger.debug(e1.getMessage());
			}
		} finally {
		}
		return TARGET_CLASS_INSTANCE;
	}

	/**
	 * Get class object of class under test
	 * 
	 * @return a {@link java.lang.Class} object.
	 */
	public static void resetTargetClass() {
		TARGET_CLASS_INSTANCE = null;
	}

	/**
	 * Update the evosuite.properties file with the current setting
	 */
	public void writeConfiguration() {
		URL fileURL = this.getClass().getClassLoader().getResource("evosuite.properties");
		String name = fileURL.getFile();
		writeConfiguration(name);
	}

	/**
	 * Update the evosuite.properties file with the current setting
	 * 
	 * @param fileName
	 *            a {@link java.lang.String} object.
	 */
	public void writeConfiguration(String fileName) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CP=");
		// Replace backslashes with forwardslashes, as backslashes are dropped during reading
		// TODO: What if there are weird characters in the code? Need regex
		buffer.append(Properties.CP.replace("\\", "/"));
		buffer.append("\nPROJECT_PREFIX=");
		if (Properties.PROJECT_PREFIX != null)
			buffer.append(Properties.PROJECT_PREFIX);
		buffer.append("\n");

		Map<String, Set<Parameter>> fieldMap = new HashMap<String, Set<Parameter>>();
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (!fieldMap.containsKey(p.group()))
					fieldMap.put(p.group(), new HashSet<Parameter>());

				fieldMap.get(p.group()).add(p);
			}
		}

		for (String group : fieldMap.keySet()) {
			if (group.equals("Runtime"))
				continue;

			buffer.append("#--------------------------------------\n");
			buffer.append("# ");
			buffer.append(group);
			buffer.append("\n#--------------------------------------\n\n");
			for (Parameter p : fieldMap.get(group)) {
				buffer.append("# ");
				buffer.append(p.description());
				buffer.append("\n");
				if (!changedFields.contains(p.key()))
					buffer.append("#");
				buffer.append(p.key());
				buffer.append("=");
				try {
					buffer.append(getStringValue(p.key()));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				buffer.append("\n\n");
			}
		}
		Utils.writeFile(buffer.toString(), fileName);
	}

	/**
	 * <p>
	 * resetToDefaults
	 * </p>
	 */
	public void resetToDefaults() {
		Properties.instance = new Properties(false, true);
		for (Field f : Properties.class.getFields()) {
			if (f.isAnnotationPresent(Parameter.class)) {
				if (defaultMap.containsKey(f)) {
					try {
						f.set(null, defaultMap.get(f));
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					}
				}
			}
		}
	}

}
