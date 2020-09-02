import {HttpClient} from '@angular/common/http';
import {PumpConfigModel} from './pump-config.model';
import {Injectable} from '@angular/core';
import {OverrideStatusEnum} from './override-status.enum';

@Injectable({providedIn: 'root'})
export class BackendHttpRequestsService {

  constructor(private http: HttpClient) {
  }

  getPumpConfiguration() {
    return this.http.get<PumpConfigModel>('http://localhost:8080/pump-configuration');
  }

  saveNewPumpConfig(newPumpConfig: PumpConfigModel){

    this.http.post('http://localhost:8080/pump-configuration/new', newPumpConfig).subscribe(response => {
      console.log(response);
    }, error => {
      console.log(error);
    });
  }
}
