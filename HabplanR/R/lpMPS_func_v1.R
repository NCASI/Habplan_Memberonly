#' Convert data frames to Habplan-suitable flow files
#'
#' Creates flow files from csv input data
#' @param std.data Forest stand, regime, and outcome information
#' @param std.info Additional stand information
#' @param col The column number for the data interested in from std.data
#' @param nyear Number of years or time periods interested in (have data for)
#' @param HSI Threshold value of HSI for species of interest (0-0.99)
#' @return Saves a .dat file in the working directory ready for import to Habplan
#' @examples
#' #Read in stand and regime information
#' std.data <- read_csv("./fvs_results.csv")
#'
#' #Read in additional stand information - acreage, id, and description
#' std.info <- read_csv("./Stand_info.csv")
#'
#' #Look at the column headings to decide which column has flow results
#' colnames(new.data)
#'
#' #From the column names, we are interested in "HSI", which is
#' #column number 37. We can input this into our function.
#'
#' #Run function
#' rcw.flow <- habConvert(std.data = new.data, std.info = std.info, col = 37,
#'                        nyear = 35, HSI = 0.7)
#'
#' @export

lpMPS <- function(std.data = std.data, nyear = "", filename = "lpMPS",
                  th.hi = 0, th.lo = 0){

  ###Need to throw an error message here so if upper and lower are not between
  ###0-100, the function will not run.###

  if(th.hi < 0 | th.hi > 100 | th.lo < 0 | th.lo > 100) {
    stop("th.hi and th.lo values must be between 0-100")
  }

  if(th.hi == 0) {
    upper <- 1.0
  } else {
    upper <- (th.hi/100)+1
  }

  if(th.lo == 0) {
    lower <- 1.0
  } else {
    lower <- 1-(th.lo/100)
  }

  #filename = "lpMPS"
  #nyear = 35
  bio1 <- read.csv("./Biol2_test.dat", sep="",
                   header = F)
  colnames(bio1) <- c("std_id", "reg_id", "weight")

  reg.list <- data.frame(unique(bio1$reg_id))
  reg.list$col <- c(1:nrow(reg.list))
  colnames(reg.list) <- c("reg_id", "col")

  std.list <- data.frame(unique(bio1$std_id))
  std.list$row <- c(1:nrow(std.list))
  colnames(std.list) <- c("std_id", "row")

  new.data <- merge(bio1, std.list, by = "std_id")
  new.data2 <- merge(new.data, reg.list, by.y = "reg_id")
  new.data2$row <- as.numeric(new.data2$row)
  new.data3 <- new.data2[order(new.data2$row, new.data2$col),]

  npoly <- 1:nrow(data.frame(unique(std.data$StandID)))
  poly.list <- paste0(" L  p", npoly)

  nyear2 <- 1:nyear
  year.listE <- paste0(" E  F", nyear2, "A", nyear2)

  nyear3 <- 1:(nyear-1)
  year.listL <- paste0(" L  F", nyear3, "H", nyear3,
                      "
 L  F", nyear3, "L", nyear3)

  weight.list <- vector("list")
  for (o in 1:nrow(new.data3)) {
  #o <- 1
    flow.obj <- paste0("      ", new.data3$row[o], "$", new.data3$col[o], "       obj      ", new.data3$weight[o])
    flow.poly <- paste0("      ", new.data3$row[o], "$", new.data3$col[o], "       p", o, "       1.0")
    flow.acc <- paste0("      ", new.data3$row[o], "$", new.data3$col[o], "       F1", "A", nyear2, "       0.0")

    row.list <- c(flow.obj, flow.poly, flow.acc)
    weight.list[[o]] <- row.list

  }

  #testing <- c(weight.list[[1:length(weight.list)]])
  testing <- unlist(weight.list)

  year.listY1 <- paste0("      F1Y1     obj       0.0",
                        "
      F1Y1     F1H1       -", upper,
                        "
      F1Y1     F1L1       ", lower,
                        "
      F1Y1     F1A1       -1.0")

  nyear4 <- 2:(nyear)
  year.listY2 <- paste0("      F1Y", nyear4, "     obj       0.0",
                        "
      F1Y", nyear4, "     F1H", nyear3, "       1.0",
                        "
      F1Y", nyear4, "     F1L", nyear3, "       -1.0",
                        "
      F1Y", nyear4, "     F1H", nyear4, "       -", upper,
                        "
      F1Y", nyear4, "     F1L", nyear4, "       ", lower,
                        "
      F1Y", nyear4, "     F1A", nyear4, "       -1.0")

  comb.year <- c(year.listY1, year.listY2)

  rhs.list.1 <- paste0("    RHS       p", npoly, "        1")
  rhs.list.2 <- paste0("    RHS       F1A", nyear2, "        0")
  rhs.list.3 <- paste0("    RHS       F1H", nyear3, "        0",
                       "
    RHS       F1L", nyear3, "        0")

  mps.file <- file(paste0("./", filename, ".mps"))

  writeLines(c(paste0('NAME          ', filename),
               "ROWS",
               " N  obj",
               poly.list,
               year.listE,
               year.listL,
               "COLUMNS",
               testing,
               comb.year,
               "RHS",
               rhs.list.1,
               rhs.list.2,
               rhs.list.3,
               "ENDATA"
               ), mps.file)

  close(mps.file)
}
