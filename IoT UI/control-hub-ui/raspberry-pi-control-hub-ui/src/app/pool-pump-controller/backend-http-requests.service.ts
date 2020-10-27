import {HttpClient} from '@angular/common/http';
import {PumpConfigModel} from './pump-config.model';
import {Injectable} from '@angular/core';
import {OverrideStatusEnum} from './override-status.enum';

@Injectable({providedIn: 'root'})
export class BackendHttpRequestsService {
  private baseUrl = window['baseUrl'];

  constructor(private http: HttpClient) {
  }

  getPumpConfiguration() {
    return this.http.get<PumpConfigModel>(this.baseUrl + '/pump-configuration');
  }

  saveNewPumpConfig(newPumpConfig: PumpConfigModel){

    this.http.post(this.baseUrl + '/pump-configuration/new', newPumpConfig).subscribe(response => {
      console.log(response);
    }, error => {
      console.log(error);
    });
  }
}
