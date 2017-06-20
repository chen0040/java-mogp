# java-mogp

Genetic Programming Framework that supports Multi-Objective Optimization

# Install 

Add the following dependency to your POM file:

```xml

```

# Usage

The sample code belows show tree-gp based multi-objective optimization which minimizes the following two objectives:

1. the mean square errors in predicting the "Mexican Hat" symbolic regression problem
2. the average tree depth of the tree-gp program generated.

```java
List<Observation> data = Tutorials.mexican_hat();
CollectionUtils.shuffle(data);
TupleTwo<List<Observation>, List<Observation>> split_data = CollectionUtils.split(data, 0.9);
List<Observation> trainingData = split_data._1();
List<Observation> testingData = split_data._2();

NSGPII tgp = NSGPII.defaultConfig();
tgp.setVariableCount(2); // the number of variables is equal to the input dimension of an observation in the "data" list
tgp.setCostFunction((CostFunction) (solution, objectiveCount, gpConfig) -> {
 List<Observation> observations = gpConfig.getObservations();
 double error = 0;
 for(Observation observation : observations){
    solution.execute(observation);
    error += Math.pow(observation.getOutput(0) - observation.getPredictedOutput(0), 2.0);
 }

 double cost1 = error;
 double cost2 = solution.averageTreeDepth();

 return Arrays.asList(cost1, cost2);
});
tgp.setMaxGenerations(50);
tgp.setPopulationSize(500);

tgp.setDisplayEvery(2); // diplay the iteration result for every 2 iterations
NondominatedPopulation pareto_front = tgp.fit(trainingData);
System.out.println("pareto_front: " + pareto_front.size());

MOOGPSolution solution = (MOOGPSolution)pareto_front.get(0);
Solution program = solution.getGp();
```

The number of variable of a tree-gp program is set by calling NSGPII.setVariableCount(...), the number of variables is equal to the 
input dimension of the problem to be solved. In the case of "Mexican Hat" symbolic regression, the input is (x, y), therefore,the 
number of variables is 2.

The cost evaluator computes two objectives. The first objective is the training cost of a tree-gp 'program' on the 'observations' 
(which is the symbolic regression trainingData), the second objective if the average depth of the tree-gp 'program'.

The last two line returns the tree-gp program associated with the first solution on the pareto-front obtained.
 
Calling program.mathExpression() will returns the math expression representing the gp program, a sample of which is shown below:

```
Trees[0]: 1.0 - (if(1.0 < if(1.0 < 1.0, if(1.0 < v0, 1.0, 1.0), if(1.0 < (v1 * v0) + (1.0 / 1.0), 1.0 + 1.0, 1.0)), 1.0, v0 ^ 1.0))
```

### Test the program obtained from the TreeGP evolution

The best program in the TreeGP population obtained from the training in the above step can then be used for prediction, as shown by the sample code below:

```java
for(Observation observation : testingData) {
 program.execute(observation);
 double predicted = observation.getPredictedOutput(0);
 double actual = observation.getOutput(0);

 logger.info("predicted: {}\tactual: {}", predicted, actual);
}
```

