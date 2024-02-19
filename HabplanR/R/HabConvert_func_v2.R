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

habConvert <- function(std.data, std.info, col, nyear, HSI = NA){

  #We need to extract year, stand id and flow

  #Get each stand name to run through loop
  std.id <- unique(std.data$StandID)

  #Create list to store stand data
  std.list <- vector(mode = "list", length = length(std.id))

  std.data.2 <- merge(std.data, std.info, by.x = "StandID", by.y = "std_id")

  for (j in 1:length(std.list)) {
    #j<-505
    temp.data <- std.data.2 %>%
      filter(StandID == std.id[j])

    regim <- unique(temp.data$RegimeKey)
    #col <- 14
    #flow.var <- temp.data[col]

    reg.list <- vector(mode = "list", length = length(regim))

    for (i in 1:length(regim)) {
      #i <- 2
      #col <- 36
      if(colnames(temp.data[col]) == "HSI") {
        hsi.data <- temp.data
        hsi.data$HSI2 <- NA
        #HSI <- 0.7
        hsi.data$HSI2[hsi.data$HSI > HSI] <- hsi.data$acres[hsi.data$HSI > HSI]
        #hsi.data <- hsi.data %>%
        #  replace(is.na(.), 0)
        pulp.data <- cbind(hsi.data[1], hsi.data$RegimeKey, hsi.data$StandID, hsi.data$HSI2)
        colnames(pulp.data) <- c("year", "regime", "std_id", "flow")
        } else {
      pulp.data <- cbind(temp.data[1], temp.data$RegimeKey, temp.data$StandID, temp.data[col])
      colnames(pulp.data) <- c("year", "regime", "std_id", "flow")
        }
      #Need to replace all of the NAs to 0s
      #library(dplyr)
      pulp.data <- pulp.data %>%
        replace(is.na(.), 0)

      #Set number of years the regimes are running for
      #nyear <- 35
      years <- c(1:nyear)
      years <- as.character(years)
      years <- toString(years)
      #Remove commas from string of years since habplan cant read them
      years <- gsub('[,]', '', years)

      #Filter by regime
      #i <- 1
      pulp.data.2 <- pulp.data %>%
        filter(regime == regim[i])

      #Now get flow results for those years
      flows <- c(pulp.data.2[(1:35), (4:4)])
      #flows
      #Get the acreage for the std_id
      std.name <- unique(pulp.data.2$std_id)
      std.in <- std.info %>%
        filter(std_id == std.name)
      acres <- std.in[[1, 2]]
      if(colnames(temp.data[col]) == "HSI"){
      flows <- flows
      } else{
      flows <- flows*acres
      }
      flows <- as.character(flows)
      flows <- toString(flows)
      #Remove commas from string of flows since habplan cant read them
      flows <- gsub('[,]', '', flows)

      id <- unique(pulp.data.2$std_id)
      reg <- unique(pulp.data.2$regime)

      data <- paste0(id, ' ', reg, ' ', years, ' ', flows)
      reg.list[[i]] <- data
    }
    vec <- unlist(reg.list)
    std.list[[j]] <- vec
  }
  final.data <- unlist(std.list)
  data.file <- file(paste0("./", colnames(temp.data[col]), ".dat"))
  writeLines(final.data, data.file)
  close(data.file)
  return(final.data)
}
