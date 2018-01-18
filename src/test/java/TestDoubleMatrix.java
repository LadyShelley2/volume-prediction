import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class TestDoubleMatrix {
    TestDoubleMatrix(){

    }
    DoubleMatrix testDiv(DoubleMatrix matrix, double number){
        return matrix.div(number);
    }
    DoubleMatrix testDiv(DoubleMatrix matrix,DoubleMatrix matrix2){
        return  matrix.div(matrix2);
    }
    DoubleMatrix testPow(DoubleMatrix matrix, double number){
        return MatrixFunctions.pow(matrix,number);
    }
    DoubleMatrix testPowi(DoubleMatrix matrix,double number){
        return MatrixFunctions.powi(matrix,number);
    }
    DoubleMatrix testAdd(DoubleMatrix matrix, DoubleMatrix matrix2){
        return matrix.add(matrix2);
    }

    public static void main(String[] args){
        TestDoubleMatrix test = new TestDoubleMatrix();
        DoubleMatrix matrix = new DoubleMatrix(new double[][]{{4,3},{9,12}});
        System.out.println("test pow: "+test.testPow(matrix,2));
        System.out.println("test powi: "+test.testPowi(matrix,2));
        System.out.println("test div: "+test.testDiv(matrix,2));

        System.out.println("test powi: "+test.testDiv(new DoubleMatrix(new double[][]{{4,4},{9,9}}),new DoubleMatrix(new double[][]{{4,2},{0.5,9}})));
        System.out.println("test add: "+ test.testAdd(matrix,matrix));
        System.out.println("test add: "+ test.testAdd(matrix,DoubleMatrix.ones(2,2)));
        System.out.println("test add: "+ test.testAdd(DoubleMatrix.rand(20,20),DoubleMatrix.ones(20,20)));
    }
}