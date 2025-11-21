## std.math: A Comprehensive Summary (including submodules)

The `std.math` module provides a collection of mathematical functions and utilities for Clojure, covering basic arithmetic, statistical calculations, random number generation, and Markov chain analysis. It aims to offer a convenient and comprehensive set of tools for numerical and probabilistic computations.

### `std.math` (Main Namespace)

This namespace serves as the primary entry point for mathematical operations, aggregating and re-exporting key functionalities from its submodules.

**Key Re-exported Functions:**

*   From `std.math.common`: `abs`, `ceil`, `factorial`, `floor`, `combinatorial`, `log`, `loge`, `log10`, `mean`, `median`, `mode`, `variance`, `stdev`, `skew`, `kurtosis`, `histogram`.
*   From `std.math.aggregate`: `aggregates`.
*   From `std.math.random`: `rand-seed!`, `rand`, `rand-int`, `rand-nth`, `rand-digits`, `rand-sample`.

### `std.math.aggregate` (Aggregation Functions)

This sub-namespace provides functions for calculating various aggregate statistics on collections of numbers.

**Key Functions:**

*   **`max-fn`**: Returns the maximum value in a collection.
*   **`min-fn`**: Returns the minimum value in a collection.
*   **`range-fn`**: Returns the range (max - min) of a collection.
*   **`middle-fn`**: Returns the middle element of a sorted collection.
*   **`wrap-not-nil`**: A helper to wrap functions that should ignore `nil` values.
*   **`+aggregations+`**: A map of common aggregation functions (first, last, middle, mean, sum, max, min, range, stdev, mode, median, skew, variance, random).
*   **`aggregates`**: Calculates a set of aggregate statistics for a collection.

### `std.math.common` (Common Mathematical Functions)

This sub-namespace provides a collection of fundamental mathematical and statistical functions.

**Key Functions:**

*   **`abs`**: Absolute value.
*   **`square`**: Squares a number.
*   **`sqrt`**: Square root.
*   **`ceil`**: Ceiling of a number.
*   **`floor`**: Floor of a number.
*   **`factorial`**: Factorial of a number.
*   **`combinatorial`**: Binomial coefficient (n choose i).
*   **`log`**: Logarithm to an arbitrary base.
*   **`loge`**: Natural logarithm.
*   **`log10`**: Base-10 logarithm.
*   **`mean`**: Arithmetic mean.
*   **`mode`**: Mode (most frequent value).
*   **`median`**: Median (middle value).
*   **`percentile`**: Calculates a percentile.
*   **`quantile`**: Splits data into quantiles.
*   **`variance`**: Sample variance.
*   **`stdev`**: Standard deviation.
*   **`skew`**: Skewness.
*   **`kurtosis`**: Kurtosis.
*   **`histogram`**: Creates a histogram.

### `std.math.markov` (Markov Chain Analysis)

This sub-namespace provides functions for working with Markov chains, including calculating cumulative probabilities and generating sequences based on a probability matrix.

**Key Functions:**

*   **`cumulative`**: Reassembles probabilities in cumulative order.
*   **`select`**: Selects an item randomly based on probabilities.
*   **`generate`**: Generates an infinite stream of tokens from a probability matrix.
*   **`tally`**: Helper for `collate`.
*   **`collate`**: Generates a probability map from a sequence of tokens.

### `std.math.random` (Random Number Generation)

This sub-namespace provides functions for generating various types of random numbers and samples, leveraging `org.apache.commons.math3.random.MersenneTwister`.

**Key Functions:**

*   **`rand-gen`**: Creates a random number generator.
*   **`rand-seed!`**: Sets the seed of a random number generator.
*   **`rand`**: Generates a random double between 0 and 1.
*   **`rand-int`**: Generates a random integer less than `n`.
*   **`rand-nth`**: Returns a random element from a collection.
*   **`rand-normal`**: Generates a random number from a normal distribution.
*   **`rand-digits`**: Generates a random n-digit string.
*   **`rand-sample`**: Samples from a collection with specified proportions.

### Usage Pattern:

The `std.math` module is useful for:
*   **Statistical Analysis:** Performing common statistical calculations on data.
*   **Data Generation:** Generating random numbers and sequences.
*   **Modeling:** Implementing probabilistic models like Markov chains.
*   **Numerical Computations:** Basic mathematical operations.

By providing a diverse set of mathematical tools, `std.math` supports various analytical and computational tasks within Clojure applications.