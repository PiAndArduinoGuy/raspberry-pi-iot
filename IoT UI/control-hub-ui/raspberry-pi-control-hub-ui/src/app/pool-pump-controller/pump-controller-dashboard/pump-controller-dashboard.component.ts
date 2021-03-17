import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { BackendHttpRequestsService } from '../backend-http-requests.service';

@Component({
  selector: 'app-pump-controller-dashboard',
  templateUrl: './pump-controller-dashboard.component.html',
  styleUrls: ['./pump-controller-dashboard.component.scss']
})
export class PumpControllerDashboardComponent implements OnInit {
  latestAmbientTempReading: number ;
  latestAvgAmbientTempReading: number;
  pumpControllerState: string;
  noAvgAmbientTempReadingYet: boolean;
  noAmbientTempReadingYet: boolean;
  noPumpControllerState: boolean;
  updateAmbientTempReadings: Subscription;
  constructor(private backendHttpRequestsService: BackendHttpRequestsService) { }

  ngOnInit(): void {
    this.performLatestAmbientTempReadingCheck();
    this.performLatestAvgAmbientTempReadingCheck();
    this.performPumpControllerStateCheck();
    this.updateAmbientTempReadings = interval(5000).subscribe(() => {
        this.performLatestAmbientTempReadingCheck();
        this.performLatestAvgAmbientTempReadingCheck();
        this.performPumpControllerStateCheck();
    })
  }

  private performLatestAmbientTempReadingCheck() {
    this.backendHttpRequestsService.getLatestAmbientTempReading().subscribe(response=>{
      this.updateAmbientTempReadings
      this.latestAmbientTempReading = response;
      this.noAmbientTempReadingYet = false;
      console.log(this.noAmbientTempReadingYet)
    },
    (error: HttpErrorResponse) =>{
      // alert-warning class applied with message that latest ambient temp reading has not yet been sent from pump controller
      if(error.error.status = 400 && error.error.detail == 'An ambient temperature has not yet been sent.'){
        console.log(error.error.detail)
        this.noAmbientTempReadingYet = true;
      } else{
        console.log(error)
      }
    });
    
  }

  private performLatestAvgAmbientTempReadingCheck() {
    this.backendHttpRequestsService.getLatestAvgAmbientTempReading().subscribe(response=>{
      this.latestAvgAmbientTempReading = response;
      this.noAvgAmbientTempReadingYet = false;
      console.log(this.noAvgAmbientTempReadingYet);
    },
    (error: HttpErrorResponse)=>{
      console.log(error.error)
      if(error.error.status = 400 && error.error.detail == '15 ambient temperature readings have not yet been captured, an average could not be calculated'){
        console.log(error.error.detail)
        this.noAvgAmbientTempReadingYet = true;
      }else{
        console.error(error);
      }
    });
    
  }

  private performPumpControllerStateCheck(){
    this.backendHttpRequestsService.getPumpControllerState().subscribe(response => {
      this.pumpControllerState = response;
      this.noPumpControllerState = false;
    },
    (error: HttpErrorResponse) => {
      console.log(error.error)
      if(error.error.status = 400 && error.error.detail == 'The pump controller state has not been sent to the control hub. The state cannot be determined.'){
        console.log(error.error.detail);
        this.noPumpControllerState = true;
      }else{
        console.error(error)
      }
    });
  }

}
